# Reactor
An extension for [Speedment](https://github.com/speedment/speedment) that make it possible to react to changes in a database and creating Materialized Object Views.

## How it works
The extension consists of two classes. The `Reactor` polls the database at a regular interval, downloading modifications to one specific table in batches. The changes are then parsed so that a set of listeners can act on them. The second class is the `MaterializedView` class. It can act as a listener on a `Reactor`, merging the changes into one materialized view. That view can then be queried to get a current state of the database without causing additional statements to be sent.

## Installation
To add reactivity to your Speedment project, simply add the following dependency to your `pom.xml`-file:
```xml
<dependency>
    <groupId>com.github.pyknic</groupId>
    <artifactId>reactor</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage
To set up a materialized view of a table `article_event`, do the following once (after initiating Speedment):
```java
final MaterializedView<ArticleEvent> articlesView 
    = new MaterializedView<>(ArticleEvent.ARTICLE_ID);

final Reactor<ArticleEvent> reactor = 
    Reactor.builder(speedment.managerOf(ArticleEvent.class))
        .withListener(articlesView)
        .build(); // Automatically starts the reactor
```

## License
This project is made available under [the Apache 2 license](http://www.apache.org/licenses/LICENSE-2.0).
