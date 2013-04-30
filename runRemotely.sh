#!/bin/bash

# ssh into machine


# pull from git


# Switch to directory
#cd "AtomicTrader"

# and run IBController
CLASSPATH=$(pwd)
echo "$CLASSPATH/IBController/IBController.jar"

cd ibGateway/IBJts
ls

java -cp "jts.jar:hsqldb.jar:jcommon-1.0.12.jar:jhall.jar:other.jar:rss.jar:$CLASSPATH/IBController/IBController.jar" "-Dsun.java2d.noddraw=true -Xmx512M -XX:MaxPermSize=128M" ibcontroller.IBGatewayController "$CLASSPATH/IBController/ibcontroller.ini" "edemo" "demouser"


# start AtomicTrader
#bash $CLASSPATH/run/AtomicTraderTradeAll.sh

# trade (paper?) strategies

