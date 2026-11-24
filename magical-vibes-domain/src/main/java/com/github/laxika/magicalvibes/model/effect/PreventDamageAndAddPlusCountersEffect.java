package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Objects;

/**
 * Static effect turning damage dealt to a creature into counters on it: on the creature itself ("this creature") or on
 * an Equipment ("equipped creature"). {@code counterType} is {@link CounterType#PLUS_ONE_PLUS_ONE} or
 * {@link CounterType#MINUS_ONE_MINUS_ONE}; {@code kind} says how the damage is stopped:
 * <ul>
 *   <li>{@link Kind#REPLACE}: "If damage would be dealt to this creature, put that many [counters] on it instead"
 *       (Phytohydra, Lichenthrope). A replacement, so it applies to damage that can't be prevented too.</li>
 *   <li>{@link Kind#PREVENT_AND_PUT}: "prevent that damage and put that many +1/+1 counters on it" (Panther Habit).
 *       Damage that can't be prevented is dealt, and the counters are still put (CR 615.12).</li>
 *   <li>{@link Kind#PREVENT_PUT_PER_PREVENTED}: "prevent that damage. Put a -1/-1 counter on this creature for each 1
 *       damage prevented this way" (Phyrexian Hydra). Damage that can't be prevented is dealt and puts nothing.</li>
 * </ul>
 *
 * @param counterType the counters put
 * @param kind        how the damage is replaced or prevented
 */
public record PreventDamageAndAddPlusCountersEffect(CounterType counterType, Kind kind) implements CardEffect {

    /** How the damage is stopped; see the record's Javadoc. */
    public enum Kind {
        REPLACE,
        PREVENT_AND_PUT,
        PREVENT_PUT_PER_PREVENTED
    }

    public PreventDamageAndAddPlusCountersEffect {
        Objects.requireNonNull(kind, "kind");
        if (counterType != CounterType.PLUS_ONE_PLUS_ONE && counterType != CounterType.MINUS_ONE_MINUS_ONE) {
            throw new IllegalArgumentException("Damage becomes +1/+1 or -1/-1 counters: " + counterType);
        }
    }

    /** "If damage would be dealt to this creature, put that many [counters] on it instead." */
    public static PreventDamageAndAddPlusCountersEffect replacingWith(CounterType counterType) {
        return new PreventDamageAndAddPlusCountersEffect(counterType, Kind.REPLACE);
    }

    /** "Prevent that damage and put that many [counters] on it." */
    public static PreventDamageAndAddPlusCountersEffect preventingAndPutting(CounterType counterType) {
        return new PreventDamageAndAddPlusCountersEffect(counterType, Kind.PREVENT_AND_PUT);
    }

    /** "Prevent that damage. Put a [counter] on this creature for each 1 damage prevented this way." */
    public static PreventDamageAndAddPlusCountersEffect preventingAndPuttingPerPrevented(CounterType counterType) {
        return new PreventDamageAndAddPlusCountersEffect(counterType, Kind.PREVENT_PUT_PER_PREVENTED);
    }
}
