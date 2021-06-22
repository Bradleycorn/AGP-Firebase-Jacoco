# AGP-Firebase-Jacoco
A minimal Android app that demonstrates an issue with Unit Tests and Code Coverage when using the following components together:
- Android Gradle Plugin (AGP) version 4.2.X
- Firebase Performance Monitoring Plugin
- JaCoCo Code Coverage plugin (latest version - 0.8.7)
- Robolectric (latest version - 4.5.1)


## Background
The release of the Android Gradle Plugin (AGP) version 4.2 (and continuing in version 4.2.1) introduced some breaking changes with jacoco code coverage, when using
Robolectric for unit tests. The issue can be resolved with some additional configuration, but that in turn introduces a new issue when using 
the Firebase Performance monitoring plugin (and possible some other plugins as well), and this new issue does not appear to have a viable solution that
allows using all of these tools together. 

## Additional Info
When using Robolectric for Unit Tests and JaCoCo for code coverage, we must configure jacoco to include "no location" classes when it runs, by adding an appropriate 
configuration to our app level `build.gradle` file:
```
tasks.withType(Test) {
    jacoco.includeNoLocationClasses = true
}
```

While using the AGP up to and including version 4.1.3, this was enough to allow jacoco to generate proper code coverage reports. 
However, starting with the release of AGP version 4.2 (and continuing with version 4.2.1), Unit tests would fail all together, with errors like the following:
```
java.lang.NoClassDefFoundError: jdk/internal/reflect/GeneratedSerializationConstructorAccessor1
	at jdk.internal.reflect.GeneratedSerializationConstructorAccessor1.newInstance(Unknown Source)
	at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:490)
	at java.base/java.io.ObjectStreamClass.newInstance(ObjectStreamClass.java:1092)
	at java.base/java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:2150)
	at java.base/java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1668)
	at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:482)
	at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:440)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.deserializeWorker(SystemApplicationClassLoaderWorker.java:153)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:121)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:71)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.run(GradleWorkerMain.java:69)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.main(GradleWorkerMain.java:74)
```

A brief Google search reveals that this issue can be solved (worked around?) by adding an additional configuration to Jacoco:
```
tasks.withType(Test) {
    jacoco.includeNoLocationClasses = true
    jacoco.excludes = ['jdk.internal.*'] // <-- Add this line to "fix" the problem
    // If you don't want to exclude everything under jdk.internal, 
    // you can just exclude jdk.internal.reflect.GeneratedSerializationConstructorAccessor1
    // that will also solve this issue.
    // Although the 2nd issue outlined below will persist.
}
```

With this in place, there are no more errors when running Unit Tests. But now a new problem emerges. 
When running the jacoco coverage report, All classes will report 0% coverage. Why?
Well, the build output will tell you:
```
> Task :app:jacocoTestReport
[ant:jacocoReport] Classes in bundle 'app' do not match with execution data. For report generation the same class files must be used as at runtime.
[ant:jacocoReport] Execution data for class com/cdi/myapplication/MyData does not match.
```

Where does this "error" come from? It seems that having the Firebase Performance Monitoring plugin added and enabled causes this. 
If you disable the plugin by commenting it out in the `app/build.gradle`, the problem goes away, and coverage reports are generated properly.

IMO, this 2nd issue is likely not the fault of the Firebase Performance Monitoring plugin, and there could be other application tools and plugins that cause 
the same or similar problems. As demonstrated in the "Work Arounds" section below, to me the issue lies with the Android Gradle Plugin and something that changed
between version 4.1.3 and version 4.2.

## Work Arounds
There are 2 ways that I have found to work around this problem:

1. Disable the Firebase Performance Monitoring plugin. This is of course less than desirable, since we want to be able to monitor performance of our app.
2. Rollback the AGP to version 4.1.3, and remove the jdk.external.* excludes from the jacoco configuration. This solves the problem, and as long as you don't 
 NEED the features and functionality of AGP 4.2, you can get by. But obviously we'd all like to use the latest version of the AGP. 
