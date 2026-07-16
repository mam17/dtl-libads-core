# libads

Android Ads library viết bằng Kotlin, kiến trúc tách rời core khỏi network cụ thể
(AdMob, Facebook, Unity Ads...). App implement lib chỉ làm việc với `AdManager`,
không bao giờ đụng trực tiếp SDK network.

## Cấu trúc

```
libads/
├── libads-core/                  # module duy nhất hiện có - KHÔNG chứa SDK network nào
│   └── src/main/java/com/libads/core/
│       ├── AdManager.kt          # interface public chính, entry point của lib
│       ├── AdUnit.kt             # mô tả 1 placement quảng cáo
│       ├── AdType.kt             # enum loại ads
│       ├── callback/             # AdResult, AdLoadCallback, AdShowCallback
│       ├── provider/AdProvider.kt# hợp đồng cho từng network adapter
│       ├── internal/             # AdManagerImpl, AdCache, lifecycle observer
│       └── util/AdLogger.kt
│
# Bước sau (chưa làm ở giai đoạn này):
├── libads-admob/                 # implement AdProvider bằng Google Mobile Ads SDK
├── libads-facebook/               # implement AdProvider bằng Audience Network SDK
```

## Tại sao tách core/provider như vậy

- `libads-core` không có dependency tới bất kỳ SDK ads nào → build nhẹ, không ép
  app phải kéo AdMob nếu chỉ dùng Facebook.
- Mỗi network là 1 module riêng implement `AdProvider`. Muốn đổi/thêm network chỉ
  cần viết module mới, không sửa gì trong core.
- App chỉ thấy `AdManager`, `AdUnit`, callback — không thấy chi tiết network → dễ
  đổi network sau này mà code app không đổi.

## Build & publish lên GitHub để app khác `implementation`

### Cách 1: JitPack (đơn giản nhất, không cần config thêm)

1. Push repo này lên GitHub, tag version: `git tag 1.0.0 && git push origin 1.0.0`
2. App khác thêm vào `settings.gradle.kts`:
   ```kotlin
   dependencyResolutionManagement {
       repositories {
           maven { url = uri("https://jitpack.io") }
       }
   }
   ```
3. Thêm dependency:
   ```kotlin
   implementation("com.github.your-github-username:libads:1.0.0")
   ```
   (JitPack tự build module `libads-core` là artifact chính nếu chỉ có 1 module;
   nếu có nhiều module, dùng `:libads:libads-core:1.0.0`)

### Cách 2: GitHub Packages (cần token, phù hợp nếu muốn giới hạn quyền truy cập)

Xem phần `publishing {}` đã cấu hình sẵn trong `libads-core/build.gradle.kts`,
chỉ cần thêm repository đích và credentials khi cần.

## Cách app implement lib sử dụng

```kotlin
// 1. Trong Application.onCreate()
AdManager.init(this) {
    // registerProvider(AdMobProvider()) // sau khi có module libads-admob
}

// 2. Định nghĩa AdUnit (thường đặt ở 1 file AdUnits.kt riêng trong app)
val homeInterstitial = AdUnit(
    id = "home_interstitial",
    type = AdType.INTERSTITIAL,
    networkAdUnitId = "ca-app-pub-xxx/yyy",
    providerName = "admob"
)

// 3. Preload sớm (ví dụ lúc splash hoặc mở màn hình trước)
AdManager.getInstance().preload(homeInterstitial)

// 4. Show khi cần
AdManager.getInstance().show(activity, homeInterstitial, object : AdShowCallback {
    override fun onAdDismissed() {
        // điều hướng tiếp sau khi đóng ad
    }
})

// 5. (khuyến nghị) tự động destroy theo lifecycle
lifecycle.addObserver(AutoDestroyLifecycleObserver(AdManager.getInstance(), listOf(homeInterstitial)))
```

## Bước tiếp theo

Đây là bộ khung core, chưa gắn network thật nào. Bước kế tiếp: tạo module
`libads-admob` implement `AdProvider`, dùng Google Mobile Ads SDK bên trong,
map `load/show/isReady/renderInto` theo API thật của SDK đó.
