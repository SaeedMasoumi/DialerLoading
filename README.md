# DialerLoading [ ![Download](https://api.bintray.com/packages/smasoumi/maven/dialer-loading/images/download.svg) ](https://bintray.com/smasoumi/maven/dialer-loading/_latestVersion)

A rotary dial loading view written in kotlin.

<p align="center">
  <img src="preview.gif" height="400" width="366.92"/>
</p>

## Installtion 

```
implementation "io.saeid:dialer-loading:1.0.0"
```

## Usage

Add `DialerLoadingView`:

```xml
    <io.saeid.dialerloading.DialerLoadingView
        android:id="@+id/rotary_dialer"
        // optional attributes                                      
        app:dialer_background_color="@color/your_color"
        app:dialer_color_start="@color/your_color"
        app:dialer_color_end="@color/your_color"
        app:dialer_digit_color="@color/your_color"
        app:dialer_triangle_color="@color/your_color"
        app:dialer_duration="time in millis"
        .../>
```

To start loading, call `dial` method:

```kotlin
rotary_dialer.dial(
       endless = true // enable endless mode (default is true)
       digits = arrayIntOf(1,2,3) // numbers to dial
)
```

To stop loading, call `hangUp` method:

```kotlin
rotary_dialer.hangUp()
```

## Credits

Inspired by : https://www.uplabs.com/posts/dailer-loading-microinteraction

## License

```
Copyright 2018 Saeed Masoumi.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
