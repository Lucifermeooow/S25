import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:rtmp_streaming/rtmp_streaming.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:wakelock_plus/wakelock_plus.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final _rtmp = TextEditingController();
  final _backend = TextEditingController();

  CameraController? _camera;
  CameraDescription? _cameraDescription;
  Timer? _statsTimer;

  bool _initialized = false;
  bool _live = false;
  bool _busy = false;
  bool _muted = false;

  String _status = 'جاهز للبث';
  String _bitrate = '--';
  String _fps = '--';
  String _rtt = '--';

  final Map<String, bool> _destinations = {
    'YouTube': true,
    'Facebook': true,
    'TikTok': true,
  };

  @override
  void initState() {
    super.initState();
    _prepareCamera();
  }

  @override
  void dispose() {
    _statsTimer?.cancel();
    _rtmp.dispose();
    _backend.dispose();
    _camera?.dispose();
    WakelockPlus.disable();
    super.dispose();
  }

  Future<void> _prepareCamera() async {
    if (_busy || _live) return;

    setState(() {
      _busy = true;
      _status = 'جاري تجهيز الكاميرا والميكروفون...';
    });

    try {
      final cameraPermission = await Permission.camera.request();
      final microphonePermission = await Permission.microphone.request();

      if (!cameraPermission.isGranted || !microphonePermission.isGranted) {
        throw Exception('يجب السماح للكاميرا والميكروفون.');
      }

      final cameras = await availableCameras();
      if (cameras.isEmpty) {
        throw Exception('لم يتم العثور على كاميرا.');
      }

      final description = _cameraDescription ?? cameras.first;
      final controller = CameraController(
        ResolutionPreset.high,
        enableAudio: true,
        androidUseOpenGL: true,
      );

      await controller.initialize(description);
      await controller.prepareForVideoStreaming();
      await controller.setAudioSettings(128 * 1024);
      await controller.setVideoSettings(bitrate: 1500 * 1024);
      await controller.setFrameRate(30);

      if (Platform.isAndroid) {
        await controller.setForceBt709Color(true);
        await controller.setRtmpShouldSendPings(true);
      }

      await _camera?.dispose();
      if (!mounted) {
        await controller.dispose();
        return;
      }

      setState(() {
        _camera = controller;
        _cameraDescription = description;
        _initialized = true;
        _status = 'الكاميرا جاهزة — أدخل RTMP واضغط GO LIVE';
      });
    } catch (e) {
      if (mounted) {
        setState(() {
          _status = 'خطأ في تجهيز الكاميرا: $e';
        });
        _showMessage('تعذر تجهيز الكاميرا. راجع الصلاحيات.');
      }
    } finally {
      if (mounted) {
        setState(() => _busy = false);
      }
    }
  }

  Future<void> _startStreaming() async {
    if (_busy || _live || _camera == null || !_initialized) return;

    final url = _rtmp.text.trim();
    if (!url.startsWith('rtmp://') && !url.startsWith('rtmps://')) {
      _showMessage('اكتب RTMP URL صحيح.');
      return;
    }

    setState(() {
      _busy = true;
      _status = 'جاري بدء البث...';
    });

    try {
      if (Platform.isAndroid) {
        await _camera!.setForceBt709Color(true);
        await _camera!.setRtmpShouldSendPings(true);
      }

      await _camera!.startVideoStreaming(
        url,
        protocol: StreamingProtocol.rtmp,
      );
      await WakelockPlus.enable();

      if (!mounted) return;
      setState(() {
        _live = true;
        _status = '🔴 LIVE — البث متصل بالسيرفر';
      });
      _startStatsPolling();
      _showMessage('تم بدء البث');
    } catch (e) {
      await WakelockPlus.disable();
      if (mounted) {
        setState(() {
          _live = false;
          _status = 'فشل بدء البث: $e';
        });
        _showMessage('فشل بدء البث. تأكد من RTMP Server.');
      }
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  Future<void> _stopStreaming() async {
    if (_busy || !_live) return;

    setState(() {
      _busy = true;
      _status = 'جاري إيقاف البث...';
    });

    try {
      await _camera?.stopVideoStreaming();
    } catch (e) {
      debugPrint('stopStreaming: $e');
    } finally {
      _statsTimer?.cancel();
      await WakelockPlus.disable();
      if (mounted) {
        setState(() {
          _live = false;
          _busy = false;
          _status = 'تم إيقاف البث';
          _bitrate = '--';
          _fps = '--';
          _rtt = '--';
        });
      }
    }
  }

  Future<void> _switchCamera() async {
    if (_camera == null || !_initialized || _live || _busy) return;

    try {
      final cameras = await availableCameras();
      if (cameras.length < 2) {
        _showMessage('الجهاز يحتوي على كاميرا واحدة فقط.');
        return;
      }

      final current = _cameraDescription;
      final next = cameras.firstWhere(
        (camera) => camera.name != current?.name,
        orElse: () => cameras.first,
      );

      final cameraName = next.name;
      if (cameraName == null || cameraName.isEmpty) {
        _showMessage('تعذر تحديد الكاميرا');
        return;
      }
      await _camera!.switchCamera(cameraName);
      if (!mounted) return;
      setState(() => _cameraDescription = next);
      _showMessage('تم تغيير الكاميرا');
    } catch (e) {
      _showMessage('تعذر تغيير الكاميرا: $e');
    }
  }

  Future<void> _toggleMute() async {
    if (_camera == null || !_initialized) return;

    try {
      final nextMuted = !_muted;
      await _camera!.setHasAudio(!nextMuted);
      if (!mounted) return;
      setState(() => _muted = nextMuted);
      _showMessage(nextMuted ? 'تم كتم الميكروفون' : 'تم تشغيل الميكروفون');
    } catch (e) {
      _showMessage('تعذر تغيير حالة الميكروفون: $e');
    }
  }

  void _startStatsPolling() {
    _statsTimer?.cancel();
    _statsTimer = Timer.periodic(const Duration(seconds: 2), (_) async {
      if (!_live || _camera == null) return;
      try {
        final dynamic stats = await _camera!.getStreamStatistics();
        if (!mounted) return;
        setState(() {
          _bitrate = _formatBitrate(stats.bitrate);
          _fps = '${stats.fps ?? '--'}';
          _rtt = stats.rttMicros == null ? '--' : '${(stats.rttMicros / 1000).round()} ms';
        });
      } catch (_) {
        // Statistics are optional; streaming continues if the endpoint does not expose them.
      }
    });
  }

  String _formatBitrate(dynamic value) {
    if (value == null) return '--';
    final kbps = (value is num ? value : num.tryParse('$value') ?? 0) / 1000;
    return '${kbps.round()} kbps';
  }

  Future<void> _connectProvider(String provider) async {
    final backend = _backend.text.trim();
    if (backend.isEmpty) {
      _showMessage('ضع رابط OAuth Backend HTTPS أولاً.');
      return;
    }

    final uri = Uri.tryParse(
      '${backend.replaceFirst(RegExp(r'/+$'), '')}/auth/$provider/start',
    );
    if (uri == null || uri.scheme != 'https') {
      _showMessage('استخدم رابط Backend يبدأ بـ HTTPS.');
      return;
    }

    try {
      final ok = await launchUrl(uri, mode: LaunchMode.externalApplication);
      if (!ok) _showMessage('تعذر فتح تسجيل الدخول.');
    } catch (e) {
      _showMessage('خطأ في تسجيل الدخول: $e');
    }
  }

  void _showMessage(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(SnackBar(content: Text(message)));
  }

  Widget _destination(String name, IconData icon) {
    return SwitchListTile(
      contentPadding: EdgeInsets.zero,
      secondary: Icon(icon),
      title: Text(name),
      subtitle: const Text('يتم التوزيع من السيرفر وليس من الهاتف'),
      value: _destinations[name] ?? false,
      onChanged: _live ? null : (value) => setState(() => _destinations[name] = value),
    );
  }

  Widget _stat(String label, String value) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
        margin: const EdgeInsets.symmetric(horizontal: 3),
        decoration: BoxDecoration(
          color: const Color(0xFF15191E),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          children: [
            Text(value, style: const TextStyle(fontWeight: FontWeight.bold)),
            const SizedBox(height: 4),
            Text(label, style: const TextStyle(color: Colors.white60, fontSize: 12)),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isReady = _initialized && _camera != null;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Stream 22'),
        centerTitle: true,
        actions: [
          IconButton(
            tooltip: 'إعادة تجهيز الكاميرا',
            onPressed: (_busy || _live) ? null : _prepareCamera,
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Container(
              height: 280,
              clipBehavior: Clip.antiAlias,
              decoration: BoxDecoration(
                color: Colors.black,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: _live ? Colors.redAccent : Colors.white12),
              ),
              child: isReady
                  ? CameraPreview(_camera!)
                  : const Center(child: CircularProgressIndicator()),
            ),
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: _live ? Colors.red.withValues(alpha: .12) : const Color(0xFF15191E),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                _status,
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: _live ? Colors.redAccent : Colors.white,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            if (_live) ...[
              const SizedBox(height: 10),
              Row(children: [_stat('Bitrate', _bitrate), _stat('FPS', _fps), _stat('RTT', _rtt)]),
            ],
            const SizedBox(height: 16),
            TextField(
              controller: _rtmp,
              enabled: !_live,
              maxLines: 2,
              decoration: const InputDecoration(
                labelText: 'Server RTMP Ingest URL',
                hintText: 'rtmp://your-server/live/stream-key',
                prefixIcon: Icon(Icons.link),
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'الهاتف يرسل بثاً واحداً إلى Red5 / Media Server. السيرفر هو المسؤول عن التوزيع للمنصات.',
              style: TextStyle(color: Colors.white54),
            ),
            const SizedBox(height: 18),
            TextField(
              controller: _backend,
              enabled: !_live,
              decoration: const InputDecoration(
                labelText: 'OAuth Backend HTTPS URL',
                hintText: 'https://your-server.example.com',
                prefixIcon: Icon(Icons.cloud_outlined),
              ),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(child: OutlinedButton(onPressed: _live ? null : () => _connectProvider('youtube'), child: const Text('YouTube'))),
                const SizedBox(width: 8),
                Expanded(child: OutlinedButton(onPressed: _live ? null : () => _connectProvider('facebook'), child: const Text('Facebook'))),
                const SizedBox(width: 8),
                Expanded(child: OutlinedButton(onPressed: _live ? null : () => _connectProvider('tiktok'), child: const Text('TikTok'))),
              ],
            ),
            const SizedBox(height: 18),
            const Text('منصات البث', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            _destination('YouTube', Icons.play_circle_fill),
            _destination('Facebook', Icons.facebook),
            _destination('TikTok', Icons.live_tv),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: (_busy || _live) ? null : _prepareCamera,
                    icon: const Icon(Icons.camera_alt),
                    label: const Text('الكاميرا'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: (_busy || _live) ? null : _switchCamera,
                    icon: const Icon(Icons.flip_camera_android),
                    label: const Text('تغيير'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            OutlinedButton.icon(
              onPressed: isReady && !_busy ? _toggleMute : null,
              icon: Icon(_muted ? Icons.mic_off : Icons.mic),
              label: Text(_muted ? 'تشغيل الميكروفون' : 'كتم الميكروفون'),
            ),
            const SizedBox(height: 14),
            SizedBox(
              height: 58,
              child: FilledButton.icon(
                onPressed: _busy ? null : (_live ? _stopStreaming : _startStreaming),
                icon: Icon(_live ? Icons.stop : Icons.live_tv),
                label: Text(
                  _live ? 'إيقاف البث' : 'GO LIVE',
                  style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
              ),
            ),
            const SizedBox(height: 18),
            const Text(
              'Stream 22 • GitHub Ready',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.white38),
            ),
          ],
        ),
      ),
    );
  }
}
