package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;

/**
 * Capability for a static ability that changes the power a creature uses while it is tapped to pay
 * a crew, saddle or station cost (CR 702.122a, 702.171a, 702.184a). Each card names the actions it
 * applies to: "crews Vehicles" is {@link Action#CREW}, "saddles Mounts" {@link Action#SADDLE},
 * "stations permanents" {@link Action#STATION} (CR 702.184c).
 */
public interface CrewAndSaddlePowerModifierEffect extends CardEffect {

    /** A keyword action that taps creatures and counts their power. */
    enum Action {
        CREW, SADDLE, STATION;

        /** "crews Vehicles and saddles Mounts". */
        public static final Set<Action> CREW_AND_SADDLE = Set.of(CREW, SADDLE);
    }

    /** The power added while paying one of {@link #actions()}. */
    int powerBonus();

    /** The actions this modifier applies to. */
    Set<Action> actions();

    /** Whether the source's effective toughness replaces its effective power for that cost. */
    default boolean usesToughnessInsteadOfPower() {
        return false;
    }

    /** Whether this modifier applies while the source pays {@code action}. */
    default boolean appliesTo(Action action) {
        return actions().contains(action);
    }
}
