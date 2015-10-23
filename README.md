# Protek608
A data aquisition Java library for the Protek 608 digital multimeter. [Visit the project homepage](http://dariancabot.com/category/projects/protek-608-dmm/) for more information about the Protek 608 digital multimeter, development blogs, and how this software has been used in the field.

## Current Release

* [Pre-release v0.9.0-alpha](https://github.com/dariancabot/Protek608/releases/tag/v0.9.0-alpha)

## Planned Releases

* Release v1.0.0 - Full protocol decoding.
* Release v1.1.0 - Enhanced data aquisition and statistics.

## Documentation

Detailed documentation on this library and implementation is coming soon. In the meantime, you can find more information on the [project home page](http://dariancabot.com/category/projects/protek-608-dmm/).

### Quick Start

Your main class may look something like this:
```
import com.dariancabot.protek608.Protek608;

public class MyNewApp
{
  public static void main(String[] args)
  {
    // Create a new Protek608 instance and set the Event Listener for receiveing data.
    Protek608 protek608 = new Protek608();
    Events events = new Events();
    protek608.setEventListener(events);
    
    // Connect to the "COM3" serial port, and start receiving data.
    protek608.connectSerialPort("COM3");
  }
}
```

Your event listener class (to handle data) may look like this:
```
import com.dariancabot.protek608.EventListener;

public class Events implements EventListener
{
  @Override
  public void dataUpdateEvent()
  {
    Double measurement = MyNewApp.protek608.data.mainValue.getValueDouble();
    System.out.println("Measurement = " + measurement);
  }
}
```

You can also access statistical data like this:
```
// Enable statistics for the main DMM value.
MyNewApp.protek608.data.mainValue.statistics.setEnabled(true);

// Now as data is received, it's included in the statistic calculations.
// ... time passes, measurements are read ...

// Let's get the average value.
MyNewApp.protek608.data.mainValue.statistics.getAverage()

// Now the user change to a different measurement, so we should reset the statistics.
MyNewApp.protek608.data.mainValue.statistics.reset();

```

## Development Environment

* This project was created with the [NetBeans](https://netbeans.org/) 8.0 IDE (some specific project files are found in the repository). 
* Developed using [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.7.
* [jSSC](https://github.com/scream3r/java-simple-serial-connector) 2.6.0 is used for serial communications.
* [JUnit](https://github.com/junit-team/junit) 4.10 and [Hamcrest](https://github.com/hamcrest/JavaHamcrest) 1.3 are used for unit testing.
