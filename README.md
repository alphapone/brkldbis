# Purpose
Brkldis is a very basic Redis protocol implementation using embedded berkeley db as backend.
It allows to use Redis client to acces and permanent storing in fast db

# Benefits
* Single jar distribution
* Fast permanent backend
* Wide range of client implementations
* No unneeded code - small code base can be verified by securirity and supported by inhause departments

# With Redis comparison
In a lot of internet sources Redis is claimed as superfast cache
But this simple project written in scala demonstrates redis perdormance is ordinary expected performance from such application because same speed is not extraodinarious.s ordinary

Running benchmark from redis build with redis (with real keys, not single value key as in articles about Redis):
```
$ ./src/redis-benchmark -t set,get -n 100000 -q -r 64 -d 100
SET: 112359.55 requests per second, p50=0.215 msec                    
GET: 114547.53 requests per second, p50=0.223 msec
```

Running benchmark with redis build brkldis (with real keys, not single value):
```
$ ./src/redis-benchmark -p 16384  -t set,get -n 100000 -q -r 64 -d 100
ERROR: Unknown command CONFIG
ERROR: failed to fetch CONFIG from 127.0.0.1:16384
WARN: could not fetch server CONFIG
SET: 118063.76 requests per second, p50=0.207 msec                    
GET: 115606.94 requests per second, p50=0.207 msec      
```

Config command is not implemented in brkldis.

So as you can see we have almost same performance as redis....

# BUILD
Run ```sbt assembly```

# INSTALL
Copy builded jar file into any preffered location and run ```java -jar $jar_locatioin/$jar```.
You can find jar using ```find . -name *.jar``` command.

You can pass ```--db``` and ```--port``` parameters.