# Color desaturation

This library allows to automatically **desaturate colors** when using **Dark theme** 
as described in [Material Docs](https://material.io/design/color/dark-theme.html#ui-application)

### Usage

Simply add this to your BaseActivity and you're done
```kotlin
abstract class BaseActivity : AppCompatActivity() {

    private var customResources: Resources? = null

    override fun getResources(): Resources {
            val res = super.getResources()
            if (customResources == null) {
                val isDarkMode = res.getBoolean(R.bool.is_dark_mode)
                customResources = DarkDesaturatedResources(isDarkMode, res)
            }
            return customResources!!
        }

}
``` 
<br>
In this example `R.bool.dark_mode` is used to detect when is using dark mode, see the sample
application for more information

## Getting started
Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Step 2. Add the dependency
```groovy
implementation 'com.github.ologe:color-desaturation:1.0.0'
```

### Screenshots
<div style="dispaly:flex">
    <img src="https://github.com/ologe/color-desaturation/blob/master/img/light_mode.png" width="49%">
    <img src="https://github.com/ologe/color-desaturation/blob/master/img/dark_mode.png" width="49%">
   
</div>