package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * A player plays a land, a special action (CR 116.2a): "whenever you play a land". Build it through
 * {@link Triggers}.
 *
 * @param relation how the land played relates to the ability's source: {@link ObjectRelation#YOU_CONTROL} for "you
 *                 play a land", {@link ObjectRelation#ANOTHER_YOU_CONTROL} for "another land",
 *                 {@link ObjectRelation#OPPONENT_CONTROLS} for "an opponent plays a land"
 * @param filter   what the land played must be ("an Island", "a Desert", "a legendary land"), or {@code null} for any
 *                 land
 */
public record LandPlayTrigger(ObjectRelation relation, CardPredicate filter) implements Trigger {

    public LandPlayTrigger {
        Objects.requireNonNull(relation, "relation");
    }

    public LandPlayTrigger(ObjectRelation relation) {
        this(relation, null);
    }

    /** This trigger for a land played that matches {@code filter} only. */
    public LandPlayTrigger withFilter(CardPredicate filter) {
        return new LandPlayTrigger(relation, Objects.requireNonNull(filter, "filter"));
    }

    /** The land-play collectors read every land-play ability by its relation; the filter is judged by them. */
    @Override
    public Trigger watchKey() {
        return filter == null ? this : new LandPlayTrigger(relation);
    }
}
