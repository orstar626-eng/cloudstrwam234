# إصلاح البناء

سبب الفشل كان عدم توافق Kotlin 2.1.0 مع مكتبة Cloudstream pre-release المبنية ببيانات Kotlin 2.4.0، إضافة إلى أخطاء في `urlEncode` وتعارض أسماء `season` و`episode` ومنشئ HomePageResponse القديم.

تم إصلاح ذلك في:

- `build.gradle.kts`: Kotlin 2.4.0.
- `OrxaProvider.kt`: `encodeUrl`, `newHomePageResponse`, وأسماء الحقول داخل Episode.
- `.github/workflows/build.yml`: بناء مباشر ورفع `.cs3` كـ Artifact.

نتيجة الاختبار المحلي: `BUILD SUCCESSFUL`.

ملف البناء الناتج: `OrxaProvider/build/OrxaProvider.cs3`.
