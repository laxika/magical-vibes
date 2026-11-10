package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Counters (CR 122.1) are put on or removed from a permanent or a player, or a keyword action that is about counters
 * happens: a permanent becomes renowned (CR 702.112b) or monstrous (CR 701.37b), evolves (CR 702.100b) or trains
 * (CR 702.149c), a Class gains a level (CR 716.2a), a player gets energy (CR 107.14) or proliferates (CR 701.34a).
 * Counters a permanent enters with are put on it too (CR 122.6). Build it through {@link Triggers}.
 *
 * @param event    what happens: which counters, put or removed, and whether a particular player must put them
 * @param relation how the permanent the counters are put on or removed from relates to the ability's source:
 *                 {@link ObjectRelation#THIS} for "this permanent", {@link ObjectRelation#YOU_CONTROL} for "a
 *                 creature you control", {@link ObjectRelation#ANOTHER_YOU_CONTROL} for "another creature you
 *                 control", {@link ObjectRelation#ANY} for "a creature", {@link ObjectRelation#ANOTHER} for "another
 *                 creature", {@link ObjectRelation#OPPONENT_CONTROLS} for "a creature you don't control" or "a creature
 *                 they control". An event about you alone (energy, proliferate, counters on a permanent or player) is
 *                 {@link ObjectRelation#ANY}
 * @param filter    what that permanent must be, or {@code null} for any permanent: a creature, a Human, a non-Hydra
 *                  creature, a Hero, a planeswalker, a Saga. The engine's counter collectors check it
 * @param qualifier what that permanent must also be, or {@code null}: "another colorless creature" is
 *                  {@code Triggers.youPutPlusOneCountersOnAnotherCreature().withFilter(new
 *                  PermanentIsColorlessPredicate())}. The counter collectors read it from the ability, through
 *                  {@code CounterAbilityReader}, so {@link #watchKey()} drops it
 * @param counterKind the one kind of counter the event is about where the event doesn't say it, or {@code null}: "the
 *                    last intervention counter" of {@link CounterEvent#YOU_REMOVE_LAST_COUNTER}. The collector reads
 *                    it from the ability as it does the qualifier, so {@link #watchKey()} drops it
 */
public record CounterTrigger(CounterEvent event, ObjectRelation relation, PermanentPredicate filter,
                             PermanentPredicate qualifier, CounterType counterKind) implements Trigger {

    public CounterTrigger {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(relation, "relation");
    }

    /** A trigger about counters of the kind the event says, on a permanent that also matches {@code qualifier}. */
    public CounterTrigger(CounterEvent event, ObjectRelation relation, PermanentPredicate filter,
                          PermanentPredicate qualifier) {
        this(event, relation, filter, qualifier, null);
    }

    /** A trigger on any permanent of {@code filter}'s type that relates to the source as {@code relation} says. */
    public CounterTrigger(CounterEvent event, ObjectRelation relation, PermanentPredicate filter) {
        this(event, relation, filter, null);
    }

    /** A trigger on any permanent that relates to the source as {@code relation} says. */
    public CounterTrigger(CounterEvent event, ObjectRelation relation) {
        this(event, relation, null, null);
    }

    /**
     * This trigger narrowed to a permanent that also matches {@code filter}, as {@link #qualifier} says: "whenever you
     * put one or more counters on a Goblin, Orc, or Army you control".
     */
    public CounterTrigger withFilter(PermanentPredicate filter) {
        Objects.requireNonNull(filter, "filter");
        if (qualifier != null) {
            throw new IllegalStateException("This counter trigger already has a filter: " + this);
        }
        return new CounterTrigger(event, relation, this.filter, filter, counterKind);
    }

    /**
     * The exile events ({@link CounterEvent#TIME_COUNTER_REMOVED_IN_EXILE},
     * {@link CounterEvent#YOU_REMOVE_TIME_COUNTER_IN_EXILE}) function from exile; every other event from the
     * battlefield.
     */
    @Override
    public AbilityZones defaultZones() {
        return event == CounterEvent.TIME_COUNTER_REMOVED_IN_EXILE
                || event == CounterEvent.YOU_REMOVE_TIME_COUNTER_IN_EXILE
                ? AbilityZones.EXILE : AbilityZones.BATTLEFIELD;
    }

    /**
     * The trigger the {@code CounterWatch} collectors read this one's abilities by: without its {@link #qualifier},
     * and, for the events no collector of their own reads, with the event whose collector reads them. "You put
     * counters on a creature you control" and "you put +1/+1 counters on this" are read by the collectors of the
     * event about any player ({@link CounterEvent#COUNTERS_PUT}, {@link CounterEvent#PLUS_ONE_COUNTERS_PUT}), "you
     * remove a time counter from this card while it's exiled" by the one of
     * {@link CounterEvent#TIME_COUNTER_REMOVED_IN_EXILE}, and "a +1/+1 counter is put on this", once for each counter,
     * by the one of {@link CounterEvent#PLUS_ONE_COUNTERS_PUT}. Those collectors judge who put or removed the counters,
     * or trigger once for each counter, from the ability's own event ({@code CounterAbilityReader}).
     */
    @Override
    public Trigger watchKey() {
        CounterEvent watched = watchedEvent();
        if (watched == event && qualifier == null && counterKind == null) {
            return this;
        }
        return new CounterTrigger(watched, relation, filter);
    }

    /** The event of {@link #watchKey()}: {@link #event}, or the event whose collector reads this one's abilities. */
    public CounterEvent watchedEvent() {
        return switch (event) {
            case YOU_PUT_COUNTERS -> relation == ObjectRelation.YOU_CONTROL ? CounterEvent.COUNTERS_PUT : event;
            case YOU_PUT_PLUS_ONE_COUNTERS -> relation == ObjectRelation.THIS ? CounterEvent.PLUS_ONE_COUNTERS_PUT : event;
            case EACH_PLUS_ONE_COUNTER_PUT -> relation == ObjectRelation.THIS ? CounterEvent.PLUS_ONE_COUNTERS_PUT : event;
            case YOU_REMOVE_TIME_COUNTER_IN_EXILE -> relation == ObjectRelation.THIS
                    ? CounterEvent.TIME_COUNTER_REMOVED_IN_EXILE : event;
            default -> event;
        };
    }

    /**
     * What a {@link CounterTrigger} triggers on. Unless a value says "each", the ability triggers once for each
     * permanent the counters are put on or removed from, however many counters that is. Unless it says "you put" or
     * "opponent puts", it doesn't matter who puts them.
     */
    public enum CounterEvent {
        /** One or more counters of any kind are put on a permanent. */
        COUNTERS_PUT,
        /** You put one or more counters of any kind on a permanent. */
        YOU_PUT_COUNTERS,
        /**
         * You put one or more counters of any kind on a permanent or a player; a proliferated player counts once for
         * each kind of counter.
         */
        YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER,
        /** An opponent puts one or more counters of any kind on a permanent that player controls. */
        OPPONENT_PUTS_COUNTERS_ON_OWN,
        /** One or more counters of any kind are removed from a permanent. */
        COUNTERS_REMOVED,
        /** One or more +1/+1 counters are put on a permanent. */
        PLUS_ONE_COUNTERS_PUT,
        /** A +1/+1 counter is put on a permanent, once for each counter. */
        EACH_PLUS_ONE_COUNTER_PUT,
        /** You put one or more +1/+1 counters on a permanent. */
        YOU_PUT_PLUS_ONE_COUNTERS,
        /** One or more +1/+1 counters are put on a permanent for the first time this turn. */
        FIRST_PLUS_ONE_COUNTERS_PUT_THIS_TURN,
        /** A +1/+1 counter is removed from a permanent, once for each counter. */
        EACH_PLUS_ONE_COUNTER_REMOVED,
        /** One or more -1/-1 counters are put on a permanent. */
        MINUS_ONE_COUNTERS_PUT,
        /** A -1/-1 counter is put on a permanent, once for each counter. */
        EACH_MINUS_ONE_COUNTER_PUT,
        /** You put one or more -1/-1 counters on a permanent. */
        YOU_PUT_MINUS_ONE_COUNTERS,
        /** You put a -1/-1 counter on a permanent, once for each counter. */
        YOU_PUT_EACH_MINUS_ONE_COUNTER,
        /**
         * You remove the last counter of a kind from a permanent: none of that kind is left on it
         * ({@link CounterTrigger#counterKind()} says which kind).
         */
        YOU_REMOVE_LAST_COUNTER,
        /** One or more loyalty counters are put on a permanent. */
        LOYALTY_COUNTERS_PUT,
        /** One or more loyalty counters are removed from a permanent. */
        LOYALTY_COUNTERS_REMOVED,
        /** You put one or more time counters on a permanent. */
        YOU_PUT_TIME_COUNTERS,
        /** One or more time counters are removed from a permanent. */
        TIME_COUNTERS_REMOVED,
        /** A time counter is removed from this card while it's exiled; functions from exile. */
        TIME_COUNTER_REMOVED_IN_EXILE,
        /** You remove a time counter from this card while it's exiled; functions from exile. */
        YOU_REMOVE_TIME_COUNTER_IN_EXILE,
        /** You put a lore counter on a permanent, once for each counter. */
        YOU_PUT_EACH_LORE_COUNTER,
        /** A permanent becomes renowned (CR 702.112b). */
        BECOMES_RENOWNED,
        /** A permanent becomes monstrous (CR 701.37b). */
        BECOMES_MONSTROUS,
        /** A creature evolves (CR 702.100b). */
        EVOLVES,
        /** A creature trains (CR 702.149c). */
        TRAINS,
        /** A Class becomes level 2 (CR 716.2a). */
        REACHES_LEVEL_TWO,
        /** A Class becomes level 3 (CR 716.2a). */
        REACHES_LEVEL_THREE,
        /** You get one or more energy counters (CR 107.14). */
        YOU_GET_ENERGY,
        /** You proliferate (CR 701.34a). */
        YOU_PROLIFERATE
    }
}
