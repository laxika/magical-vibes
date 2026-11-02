package com.github.laxika.magicalvibes.model.trigger;

import java.util.Objects;

/**
 * A player plays a land, a special action (CR 116.2a): "whenever you play a land". Build it through
 * {@link Triggers}.
 *
 * @param relation how the land played relates to the ability's source: {@link ObjectRelation#YOU_CONTROL} for "you
 *                 play a land", {@link ObjectRelation#ANOTHER_YOU_CONTROL} for "another land",
 *                 {@link ObjectRelation#OPPONENT_CONTROLS} for "an opponent plays a land"
 */
public record LandPlayTrigger(ObjectRelation relation) implements Trigger {

    public LandPlayTrigger {
        Objects.requireNonNull(relation, "relation");
    }
}
