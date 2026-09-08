# Stream 22 Pro - Multistream Studio

> Flutter build target: the `android/` directory. The root `app/` module is a separate native Android module kept for the existing source set.

Official Real Mobile Multistreaming Application for Android.

## Features & Architecture

- **Real YouTube Live Integration**: Google OAuth 2.0 + YouTube Data API v3 (`liveBroadcasts.insert`, `liveStreams.insert`, `liveBroadcasts.bind`).
- **Real Facebook Live Integration**: Meta Graph API v19.0 (`/me/live_videos`).
- **Real Twitch Integration**: Twitch Helix API user validation.
- **TikTok Stream Key Ingest Support**: Zero-permission direct RTMP key binding.
- **Secure KeyStore Storage**: `flutter_secure_storage` with Android KeyStore encryption.
- **Zero-Fake Architecture**: Real live lifecycle, real account info, zero simulated data in production.

## Important architecture

The Android app publishes **one** RTMP stream to your media server. The server is responsible for fan-out to YouTube, Facebook and TikTok. The destination switches in the app are configuration/UI only; they do not fake provider APIs.

OAuth client secrets, access tokens, refresh tokens and stream keys must stay on the backend. Do not commit them to GitHub.

## Local build (Optional)

```bash
flutter pub get
flutter analyze
flutter test
flutter build apk --release
```

## GitHub Actions Cloud Build ($0 / Free)

1. Create an empty GitHub repository.
2. Copy the contents of this folder into it.
3. Push to the `main` branch.
4. Open **Actions** → **Build Stream 22 Release APK**.
5. Download the `Stream22-release-apk` artifact after a successful build.

## Embedded public OAuth configuration

The existing public Google and Facebook identifiers from the source repository are centralized in:
`lib/config/platform_oauth_config.dart`

Google:
- Web client ID: `18650771866-gp6bbiqdrtba00bqcb2eic8i55hoeqoj.apps.googleusercontent.com`
- Server client ID used by `google_sign_in`: `18650771866-pj87a85tfl7vkrs0ij926n6q2ubqtdd3.apps.googleusercontent.com`

Facebook:
- App ID: `1041366175430588`

These are public identifiers and are safe to ship in the client. Provider secrets, access/refresh tokens and stream keys are not embedded.
