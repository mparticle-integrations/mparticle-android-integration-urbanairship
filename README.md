## Urban Airship Kit Integration

This repository contains the [Urban Airship](https://www.urbanairship.com) integration for the [mParticle Android SDK](https://github.com/mParticle/mparticle-android-sdk).

### Adding the integration

1. Add the kit dependency to your app's build.gradle:

    ```groovy
    dependencies {
        compile 'com.mparticle:android-urbanairship-kit:5+'
    }
    ```
2. Follow the mParticle Android SDK [quick-start](https://github.com/mParticle/mparticle-android-sdk), then rebuild and launch your app, and verify that you see `"Urban Airship detected"` in the output of `adb logcat`.
3. Reference mParticle's integration docs below to enable the integration.
4. If you wish to utilize Urban Airship's Push Messaging capabilities, please refer to the Push Message Considerations section below

### Documentation

[Urban Airship integration](http://docs.mparticle.com/?java#urban-airship)

### License

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)