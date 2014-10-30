anann
=====

Java library for execution of discrete events simulations, for Java v1.6 or higher.

License
=======
Anann is released under the [GNU General Public License v3.0](http://www.gnu.org/copyleft/gpl.html).

Dependencies
============
Anann has only one external dependence, which you can easily get rid of.

Anann can store its events in different structures, as long as they implement the `org.anann.core.events.holder.EventsHolder` interface. Some implementations are already provided inside the `org.anann.core.events.holder` package. The `EHBasedOnGuavaTreeMultimap` class, as its name suggests, uses a `com.google.common.collect.TreeMultimap` instance to store events. Thus, you will need [Google's Guava](https://code.google.com/p/guava-libraries/) (I have used v14.0.1) to compile that class. Other implementations of `EventsHolder` are also available, so in case you do not want to include extra dependencies in your system you can just ignore that class even removing it, and use any of the other implementations.

How to use it
=============
There is an example of how to use this software in the `org.anann.tests.PipeSimulation.java` file. 

FAQ
===
**What does 'Anann' mean?** I am fan of Tolkien works :) ! . Anann is _"anann is a Sindarin word for a long time, as in Cuio anann, 'Long live [them]'"_ ([Tolkien Gateway](http://tolkiengateway.net/wiki/Anann)). And anann is intended to help you to manage precisely that: time (in your simulations).

