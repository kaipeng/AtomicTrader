#!/bin/bash

# and run IBController
#starting VNC Server
vncserver :0
export DISPLAY=:0
CLASSPATH=$(pwd)
echo "Running IBController: $CLASSPATH/IBController/IBController.jar"

cd ibGateway/IBJts

java -cp "jts.jar:hsqldb.jar:jcommon-1.0.12.jar:jhall.jar:other.jar:rss.jar:$CLASSPATH/IBController/IBController.jar" "-Dsun.java2d.noddraw=true -Xmx512M -XX:MaxPermSize=128M" ibcontroller.IBGatewayController "$CLASSPATH/IBController/IBController.ini" $1 $2
