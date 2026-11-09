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
 * @param exiledWithSource whether the land must have been played from exile while exiled with the ability's source
 *                         ("whenever a player plays a card exiled with this creature"): the permanent or the face-up
 *                         plane the land-play collector reads from the event, since the land has left exile by then
 */
public record LandPlayTrigger(ObjectRelation relation, CardPredicate filter, Zone playedFrom, Zone notPlayedFrom,
                              boolean exiledWithSource) implements Trigger {

    public LandPlayTrigger {
        Objects.requireNonNull(relation, "relation");
        if (playedFrom != null && notPlayedFrom != null) {
            throw new IllegalArgumentException("A land-play trigger names the zone the land is played from or one it "
                    + "isn't");
        }
        if (exiledWithSource && playedFrom != Zone.EXILE) {
            throw new IllegalArgumentException("A land exiled with the source is played from exile");
        }
    }

    public LandPlayTrigger(ObjectRelation relation, CardPredicate filter, Zone playedFrom, Zone notPlayedFrom) {
        this(relation, filter, playedFrom, notPlayedFrom, false);
    }

    public LandPlayTrigger(ObjectRelation relation) {
        this(relation, null, null, null);
    }

    /** This trigger for a land played that matches {@code filter} only. */
    public LandPlayTrigger withFilter(CardPredicate filter) {
        return new LandPlayTrigger(relation, Objects.requireNonNull(filter, "filter"), playedFrom, notPlayedFrom,
                exiledWithSource);
    }

    /** This trigger for a land played from {@code zone} only: "whenever you play a land from exile". */
    public LandPlayTrigger playedFrom(Zone zone) {
        return new LandPlayTrigger(relation, filter, Objects.requireNonNull(zone, "zone"), notPlayedFrom,
                exiledWithSource);
    }

    /**
     * This trigger for a land not played from {@code zone} only: "whenever you play a land from anywhere other than
     * your hand".
     */
    public LandPlayTrigger notPlayedFrom(Zone zone) {
        return new LandPlayTrigger(relation, filter, playedFrom, Objects.requireNonNull(zone, "zone"),
                exiledWithSource);
    }

    /**
     * This trigger for a land played from exile while it was exiled with the ability's source only: "whenever you play
     * a card exiled with this creature", "whenever you play a land from among cards exiled with The Matrix of Time".
     */
    public LandPlayTrigger exiledWithThis() {
        return new LandPlayTrigger(relation, filter, Zone.EXILE, notPlayedFrom, true);
    }

    /**
     * Whether this trigger narrows the land played: by a filter, by the zone it is played from, or by the source it was
     * exiled with.
     */
    public boolean filtered() {
        return filter != null || playedFrom != null || notPlayedFrom != null || exiledWithSource;
    }

    /**
     * This trigger without its filters: the trigger the land filter and zones narrow. A filtered land-play trigger is
     * its own {@link #watchKey()}: the land-play collectors read such an ability itself (its filters, options and
     * effects), found by this trigger.
     */
    public LandPlayTrigger unfiltered() {
        return filtered() ? new LandPlayTrigger(relation) : this;
    }
}
