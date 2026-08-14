# ORXA Cloudstream Extension

إضافة Cloudstream لموقع ORXA: https://orxa.vercel.app/

## الوظائف المدعومة

- الصفحة الرئيسية من `/api/movies?lang=ar`.
- البحث المحلي داخل العناصر التي يعيدها API الصفحة الرئيسية.
- تفاصيل الأفلام والمسلسلات من `/api/movie`.
- المواسم والحلقات للمسلسلات عندما يعيدها API في حقل `details.seasons`.
- محاولة تحميل مصدر VidSrc بواسطة Cloudstream Extractor.

## البناء

يجب استخدام JDK 17 وAndroid SDK. نفّذ:

```bash
chmod +x gradlew
./gradlew OrxaProvider:make
```

ملف الإضافة الناتج يكون داخل مجلد `OrxaProvider/build/`.

## النشر كمستودع Cloudstream

ارفع المشروع إلى GitHub مع فرعي `main` و`builds`. بعد تشغيل GitHub Actions، سيُنشأ ملف `plugins.json` وملف `.cs3` داخل فرع `builds`. أضف رابط `plugins.json` إلى Cloudstream عبر:

```text
Settings → Extensions → Add Repository
```

## ملاحظات قانونية وتقنية

يجب استخدام الإضافة مع المصادر والمحتوى الذي تملك حق الوصول إليه أو توزيعه. رابط VidSrc الموجود في الكود هو موضع تكامل قابل للاستبدال، وقد يتوقف أو لا يعمل إذا تغير المصدر أو لم يتوفر Extractor مناسب في نسخة Cloudstream المستخدمة.

المشروع مبني على قالب Cloudstream الرسمي. بيئة التطوير الحالية لا تحتوي على Android SDK محليًا، لذلك تم تجهيز Workflow للبناء على GitHub Actions.
