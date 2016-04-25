/**
 *
 * Copyright (c) 2006-2016, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.pyknic.reactor;

import com.speedment.field.ComparableField;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A materialized view holds a snapshot of the current state of a table in the
 * database. The table rows are considered events that refer to entities. If 
 * multiple events refer to the same entity, the entities willbe merged into the 
 * state so that each entity only exist once. This is useful to create an Event 
 * Sourcing system from a regular relational database.
 * <p>
 * <b>Example usage:</b>
 * <code>
 *      final MaterializedView&lt;ArticleEvent&gt; articlesView 
 *          = new MaterializedView&lt;&gt;(ArticleEvent.ARTICLE_ID);
 * 
 *      final Reactor&lt;ArticleEvent&gt; reactor = 
 *          Reactor.builder(
 *                  speedment.managerOf(ArticleEvent.class),
 *                  ArticleEvent.ARTICLE_ID
 *          ).withListener(articlesView).build();
 * 
 *      // The reactor is automatically started.
 * </code>
 * 
 * @author          Emil Forslund
 * @param <ENTITY>  the entity type
 * @param <T>       the unique key type
 */
public class MaterializedView<ENTITY, T extends Comparable<T>> implements Consumer<List<ENTITY>> {
    
    private final ComparableField<ENTITY, ?, T> field;
    private final Map<T, ENTITY> view;
    
    /**
     * Creates a {@code MaterializedView} of the entities of a particular table. 
     * For the view to be able to distinguish which events are referring to the 
     * same object, a field identifier must be specified. Database rows that has 
     * the same value for this field will be considered the same entity and a
     * merge attempt will be done using the
     * {@link #merge(Object, Object)}-method.
     * 
     * @param field  the field to use as identifier
     */
    public MaterializedView(ComparableField<ENTITY, ?, T> field) {
        this.field = requireNonNull(field);
        this.view = new ConcurrentHashMap<>();
    }

    /**
     * Accepts a series of modifications into this view. This should only be 
     * called by the owner of the view, normally a {@link Reactor}. The events
     * given to this method is expected to be ordered chronologically.
     * 
     * @param events  the events to load
     */
    @Override
    public final void accept(List<ENTITY> events) {
        events.forEach(entity -> {
            view.compute(field.get(entity), 
                (key, existing) -> existing == null 
                    ? entity : merge(existing, entity)
            );
        });
    }
    
    /**
     * Returns the entity currently mapped to the specified value for the field
     * on which this view is identified.
     * 
     * @param key  the field value to look for
     * @return     the entity found or an empty {@code Optional}
     */
    public final Optional<ENTITY> get(T key) {
        return Optional.ofNullable(view.get(key));
    }
    
    /**
     * returns a stream of all the entities currently in the view.
     * 
     * @return  the stream
     */
    public final Stream<ENTITY> stream() {
        return view.values().stream();
    }
    
    /**
     * Merge the two specified entities into the view. The returned value could
     * be either one of the two or a completely new one. This method is 
     * guaranteed to be called in order, merging each entity with the 
     * immediately preceeding one.
     * 
     * @param existing  the existing entity
     * @param loaded    the loaded entity
     * @return          the one entity to remember in the view
     */
    protected ENTITY merge(ENTITY existing, ENTITY loaded) {
        return loaded;
    }
}