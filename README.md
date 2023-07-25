# CommandsRunner

A Velocity plugin that starts and stop backend servers based on activity. I wrote this for my personal server.

Executing commands from the JVM, especially as root using sudo might be a little bit janky, and unless you are running in an enviromnent where it's somewhat OK, like a VM, I would highly
advise against running something like this.