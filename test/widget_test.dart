import 'package:flutter_test/flutter_test.dart';
import 'package:stream_22/main.dart';

void main() {
  testWidgets('Stream 22 renders', (WidgetTester tester) async {
    await tester.pumpWidget(const Stream22App());
    expect(find.byType(Stream22App), findsOneWidget);
  });
}
