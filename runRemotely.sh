#!/bin/bash

# ssh into machine
ssh 

# pull from git


# Switch to directory
#cd "AtomicTrader"

# and run IBController
CLASSPATH=$(pwd)
echo "Running IBController: $CLASSPATH/IBController/IBController.jar"

cd ibGateway/IBJts

java -cp "jts.jar:hsqldb.jar:jcommon-1.0.12.jar:jhall.jar:other.jar:rss.jar:$CLASSPATH/IBController/IBController.jar" "-Dsun.java2d.noddraw=true -Xmx512M -XX:MaxPermSize=128M" ibcontroller.IBGatewayController "$CLASSPATH/IBController/IBController.ini" $1 $2


# start AtomicTrader
#bash $CLASSPATH/run/AtomicTraderTradeAll.sh

# trade (paper?) strategies

