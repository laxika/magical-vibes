package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.Zone;
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
 * @param playedFrom    the zone the land must have been played from ("a land from exile", CR 116.2a), or
 *                      {@code null} for any
 * @param notPlayedFrom a zone the land must not have been played from ("a land from anywhere other than your hand"),
 *                      or {@code null} for none
 */
public record LandPlayTrigger(ObjectRelation relation, CardPredicate filter, Zone playedFrom, Zone notPlayedFrom)
        implements Trigger {

    public LandPlayTrigger {
        Objects.requireNonNull(relation, "relation");
        if (playedFrom != null && notPlayedFrom != null) {
            throw new IllegalArgumentException("A land-play trigger names the zone the land is played from or one it "
                    + "isn't");
        }
    }

    public LandPlayTrigger(ObjectRelation relation) {
        this(relation, null, null, null);
    }

    /** This trigger for a land played that matches {@code filter} only. */
    public LandPlayTrigger withFilter(CardPredicate filter) {
        return new LandPlayTrigger(relation, Objects.requireNonNull(filter, "filter"), playedFrom, notPlayedFrom);
    }

    /** This trigger for a land played from {@code zone} only: "whenever you play a land from exile". */
    public LandPlayTrigger playedFrom(Zone zone) {
        return new LandPlayTrigger(relation, filter, Objects.requireNonNull(zone, "zone"), notPlayedFrom);
    }

    /**
     * This trigger for a land not played from {@code zone} only: "whenever you play a land from anywhere other than
     * your hand".
     */
    public LandPlayTrigger notPlayedFrom(Zone zone) {
        return new LandPlayTrigger(relation, filter, playedFrom, Objects.requireNonNull(zone, "zone"));
    }

    /** The land-play collectors read every land-play ability by its relation; the filters are judged by them. */
    @Override
    public Trigger watchKey() {
        return filter == null && playedFrom == null && notPlayedFrom == null ? this : new LandPlayTrigger(relation);
    }
}
