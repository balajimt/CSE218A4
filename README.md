# CSE218 - Assignment 4
## WearOs app - Heart rate sensor
### Authors: Balaji Muthazhagan (UCSD), Shikha Dixit (UCSD)
___
### Functionalities
The app has the following functionalities:
* Shows current latitude, longitude and altitude on screen
* Shows current Wifi strength and SSID
* Shows current heart rate
* Shows approximate number of steps based on Accelerometer data

<br><br>
The main functionality of the app is defined in `main/wear/src/main/java/CSE218/MainActivity.java`

The heart rate listener is defined in `main/wear/src/main/java/CSE218/HeartRateListener.java`

The step count listener is defined in `main/wear/src/main/java/CSE218/StepCountListener.java`
<br><br><br>

### Step counter algorithm
The step counter algorithm was largely inspired from [the MathWorks counting steps documentation](https://www.mathworks.com/help/matlabmobile/ug/counting-steps-by-capturing-acceleration-data.html). It is defined as follows:
* First the gravity component is removed from the raw accelerometer data with a low pass filter which provides some smoothing.
* Thereafter a z-scoretime series peak detection algorithm adapted from [StackOverflow](https://stackoverflow.com/questions/22583391/peak-signal-detection-in-realtime-timeseries-data). In summary, the raw z score is defined as `z = (x-m)/s` where m is the mean, s is the standard deviation and x is the input. The algorithm samples a certain number of inputs and maintains a average for the minimum number of sampled inputs. The mean and standard deviation gets updated for the next window and gets repeated for the next set of inputs. The peaks detected in the process are the steps detected among the inputs sampled.

### Output screens
<b>Output capture without steps</b><br><br>
![output](output_images/output.gif)

<b><br><br>Output capture with steps</b><br><br>
![output](output_images/output_withsteps.gif)

