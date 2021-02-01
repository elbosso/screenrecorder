# screenrecorder

<!---
[![start with why](https://img.shields.io/badge/start%20with-why%3F-brightgreen.svg?style=flat)](http://www.ted.com/talks/simon_sinek_how_great_leaders_inspire_action)
--->
[![GitHub release](https://img.shields.io/github/release/elbosso/screenrecorder/all.svg?maxAge=1)](https://GitHub.com/elbosso/screenrecorder/releases/)
[![GitHub tag](https://img.shields.io/github/tag/elbosso/screenrecorder.svg)](https://GitHub.com/elbosso/screenrecorder/tags/)
[![GitHub license](https://img.shields.io/github/license/elbosso/screenrecorder.svg)](https://github.com/elbosso/screenrecorder/blob/master/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/elbosso/screenrecorder.svg)](https://GitHub.com/elbosso/screenrecorder/issues/)
[![GitHub issues-closed](https://img.shields.io/github/issues-closed/elbosso/screenrecorder.svg)](https://GitHub.com/elbosso/screenrecorder/issues?q=is%3Aissue+is%3Aclosed)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/elbosso/screenrecorder/issues)
[![GitHub contributors](https://img.shields.io/github/contributors/elbosso/screenrecorder.svg)](https://GitHub.com/elbosso/screenrecorder/graphs/contributors/)
[![Github All Releases](https://img.shields.io/github/downloads/elbosso/screenrecorder/total.svg)](https://github.com/elbosso/screenrecorder)
[![Website elbosso.github.io](https://img.shields.io/website-up-down-green-red/https/elbosso.github.io.svg)](https://elbosso.github.io/)

## Overview

This project offers a screencast recorder that outputs avi files and a screenshot tool that saves
png files. It opens a red frame that can be freely placed anywhere on the Desktop and resized and defines the area that
is going to reccorded.
You can build it by issuing

```
mvn compile package
```

and then starting the resulting monolithic jar file by issuing

```
$JAVA_HOME/bin/java -jar target/screenrecorder-<version>-jar-with-dependencies.jar
```

Alternatively one could just start the app using maven by  issuing

```
mvn compile exec:java
```


