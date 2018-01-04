# CircularSeekBar [ ![Download](https://api.bintray.com/packages/akaita/android/circular-seek-bar/images/download.svg) ](https://bintray.com/akaita/android/circular-seek-bar/_latestVersion)

This is a simple Seek Bar, in the shape of a circle, responsive to the speed of the spin: the faster you spin the control, the faster the progress increases.

Very much in the style of old iPods, this is a very intuitive control when a value from a variable range has to be chosen, and for wearable apps. It has been used in a production app for a while now, so it is well proven.

## Screenshots

| In a mobile device | In a wearable device |
| - | - |
| ![mobile](https://github.com/paxku/CircularSeekBar/blob/master/screenshots/mobile.gif "In a mobile phone") | ![wearable](https://github.com/paxku/CircularSeekBar/blob/master/screenshots/wear.png "In a wearable device") |

## Usage

```groovy
repositories {
    jcenter()
}
dependencies {
    compile 'com.akaita.android:circular-seek-bar:1.0'
}
```

## Configuration

### xml && programmatic

```java
min="0"    //Minimum progree value
max="50"       //Maximum progress value
progress="15" //Current progress value
progressTextColor="@android:color/black"    //Color for the text in the center
progressTextSize="26"    //Size for the text in the center
showProgressText="true"    //Show/hide the text in the center
progressText="Custom text"    //Show custom text in the center
ringColor="@color/colorAccent"    //Color for the outer ring
ringWidth="0.5"    //Width of the outer ring, relative to the width of the whole view
showIndicator="true"    //Show/hide the arc drawn when the user touches the ring
showInnerCircle="true"    //Show/hide the circle in the center
speedMultiplier="2"    //Make the progress increase/decrease faster/slower
```

### only programmatic

```java
OnCircularSeekBarChangeListener    //Listener for events changing the progress
OnCenterClickedListener    //Listener for single tap events on the inner circle
RingPaint    //Paint used to draw the outer ring
InnerCirclePaint    //Paint used to draw the inner circle
ProgressTextPaint    //Paint used to draw the text in the center
ProgressTextFormat    //Format of the text in the center
```

## Apps using CircularSeekBar

 - [F-gas](https://play.google.com/store/apps/details?id=com.akaita.fgas): a simple tool to help you comply with the new EU Regulation 517/2014 on fluorinated gases with just one hand!
