package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Counters were put on or removed from a permanent, a player or a card in exile (CR 122.1), or a keyword action about
 * counters happened (proliferate, renown, monstrosity, training, evolve, a Class gaining a level, a player getting
 * {E}): the events the counter abilities trigger on (CR 603.2). Each is an occurrence of its own, emitted as the
 * counters change. The permanents an event names are the live objects, which the abilities read as they trigger; a
 * simulation copy of the event copies them.
 *
 * @param kind          what happened
 * @param permanent     the permanent the counters were put on or removed from, or the one the keyword action is
 *                      about; {@code null} for an event about a player
 * @param permanents    the permanents of an event that put counters on several at once
 * @param card          the exiled card a time counter was removed from
 * @param playerId      the player who put or removed the counters, or the player the event is about
 * @param controllerId  the controller of {@code permanent} as the event happened, for the kinds that name one
 * @param counterType   the kind of counters, or {@code null} when the event doesn't say
 * @param amount        how many counters (or {E}, or proliferations; an X for monstrosity)
 * @param previousCount how many counters of the kind the permanent had before
 * @param first         for the kinds that say so, whether this is the first such placement on the permanent this turn
 * @param excluded      the permanents whose abilities don't see this placement (the sources that made it)
 */
public record CounterChangeEvent(Kind kind, Permanent permanent, List<Permanent> permanents, Card card,
                                 UUID playerId, UUID controllerId, CounterType counterType, int amount,
                                 int previousCount, boolean first, List<Permanent> excluded) implements RulesEvent {

    public CounterChangeEvent {
        Objects.requireNonNull(kind, "kind");
        permanents = permanents == null ? List.of() : List.copyOf(permanents);
        excluded = excluded == null ? List.of() : List.copyOf(excluded);
    }

    /** An event of {@code kind} about {@code permanent}, put on or removed by {@code playerId}. */
    public static CounterChangeEvent of(Kind kind, Permanent permanent, UUID playerId, CounterType counterType,
                                        int amount) {
        return new CounterChangeEvent(kind, permanent, null, null, playerId, null, counterType, amount, 0, false,
                null);
    }

    /** An event of {@code kind} about the player {@code playerId} only. */
    public static CounterChangeEvent ofPlayer(Kind kind, UUID playerId, int amount) {
        return new CounterChangeEvent(kind, null, null, null, playerId, null, null, amount, 0, false, null);
    }

    /** This event with {@code controllerId} as the permanent's controller as it happened. */
    public CounterChangeEvent withController(UUID controllerId) {
        return new CounterChangeEvent(kind, permanent, permanents, card, playerId, controllerId, counterType, amount,
                previousCount, first, excluded);
    }

    /** This event with {@code previousCount} and {@code first}. */
    public CounterChangeEvent withPrevious(int previousCount, boolean first) {
        return new CounterChangeEvent(kind, permanent, permanents, card, playerId, controllerId, counterType, amount,
                previousCount, first, excluded);
    }

    /** This event naming {@code permanents} and the {@code excluded} sources. */
    public CounterChangeEvent withPermanents(List<Permanent> permanents, List<Permanent> excluded) {
        return new CounterChangeEvent(kind, permanent, permanents, card, playerId, controllerId, counterType, amount,
                previousCount, first, excluded);
    }

    /** This event about the exiled {@code card}. */
    public CounterChangeEvent withCard(Card card) {
        return new CounterChangeEvent(kind, permanent, permanents, card, playerId, controllerId, counterType, amount,
                previousCount, first, excluded);
    }

    @Override
    public CounterChangeEvent deepCopy() {
        return new CounterChangeEvent(kind, permanent == null ? null : new Permanent(permanent),
                permanents.stream().map(Permanent::new).toList(), card, playerId, controllerId, counterType, amount,
                previousCount, first, excluded.stream().map(Permanent::new).toList());
    }

    /** What happened. */
    public enum Kind {
        /** Counters of {@code counterType} ({@code null}: unknown) were put on {@code permanent} by {@code playerId}. */
        PLACED,
        /** {@code playerId} put {@code amount} counters on a permanent or player. */
        YOU_PUT_COUNTERS,
        /** {@code playerId} put {@code amount} time counters on {@code permanent}, a permanent they control. */
        YOU_PUT_TIME_COUNTERS,
        /** {@code playerId} put a lore counter on {@code permanent}, a Saga they control. */
        YOU_PUT_LORE_COUNTER,
        /** Counters were put on {@code permanent} itself: its own "whenever counters are put on this". */
        SELF_PLACED,
        /** {@code playerId} put counters on {@code permanent}, a creature, seen by their other permanents. */
        YOU_PUT_ON_ANOTHER_CREATURE,
        /** {@code playerId} put counters on {@code permanent}, a creature they control, seen by their opponents. */
        OPPONENT_PUTS_ON_OWN_CREATURE,
        /** {@code playerId} put +1/+1 counters on the Heroes {@code permanents} at once. */
        PLUS_ONE_ON_OTHER_HERO,
        /** {@code playerId} put {@code amount} +1/+1 counters on {@code permanent}, a creature, seen by their others. */
        PLUS_ONE_ON_ANOTHER_CREATURE,
        /** A +1/+1 counter placement on {@code permanent}: its own, its controller's other creatures', Humans'. */
        PLUS_ONE_PLACED,
        /** A +1/+1 counter placement on {@code permanent} itself, by its controller. */
        PLUS_ONE_ON_SELF,
        /** {@code amount} +1/+1 counters on {@code permanent}, seen by its controller's other permanents. */
        PLUS_ONE_ON_ANOTHER_NON_HYDRA,
        /** {@code amount} +1/+1 counters on {@code permanent}, which {@code controllerId} controls. */
        PLUS_ONE_ON_CONTROLLED_PERMANENT,
        /** {@code amount} +1/+1 counters on {@code permanent}, a creature, seen from its controller's graveyard. */
        GRAVEYARD_PLUS_ONE_ON_CREATURE,
        /** {@code amount} counters on {@code permanent}, a creature, seen by its controller's permanents. */
        ON_CONTROLLED_CREATURE,
        /** {@code amount} -1/-1 counters on {@code permanent}, a creature, put by {@code playerId}. */
        MINUS_ONE_PLACED,
        /** {@code amount} loyalty counters on planeswalkers {@code playerId} controls. */
        LOYALTY_ON_PLANESWALKERS,
        /** {@code playerId} removed the last counter of {@code counterType} from {@code permanent}. */
        LAST_REMOVED,
        /**
         * Counters were removed from permanents since the abilities last looked: each permanent keeps the counters
         * removed from it (all kinds, time, +1/+1 and loyalty counters) until this event reads them.
         */
        REMOVED,
        /** A time counter was removed from the exiled {@code card}, {@code amount} remaining, by {@code playerId}. */
        TIME_COUNTER_REMOVED_IN_EXILE,
        /** {@code permanent}, which {@code playerId} controls, became renowned. */
        RENOWNED,
        /** {@code permanent}, which {@code playerId} controls, became monstrous with X {@code amount}. */
        MONSTROUS,
        /** {@code permanent} trained. */
        TRAINED,
        /** {@code permanent} evolved. */
        EVOLVED,
        /** {@code permanent}, a Class, became level {@code amount}. */
        LEVEL_REACHED,
        /** {@code playerId} got {@code amount} {E}. */
        ENERGY,
        /** {@code playerId} proliferated {@code amount} times. */
        PROLIFERATED
    }
}
