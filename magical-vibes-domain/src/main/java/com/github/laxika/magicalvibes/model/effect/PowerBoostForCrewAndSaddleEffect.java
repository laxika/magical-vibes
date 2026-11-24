package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;

/**
 * Static pilot-style power bonus used only when the creature taps for one of {@code actions}:
 * "crews Vehicles [and saddles Mounts / and stations permanents] as though its power were N greater".
 *
 * @param actions the keyword actions it applies to (crew and saddle by default; crew only for
 *                "crews Vehicles"; crew and station for Stoic Star-Captain)
 */
public record PowerBoostForCrewAndSaddleEffect(int powerBonus, Set<Action> actions)
        implements CrewAndSaddlePowerModifierEffect {

    public PowerBoostForCrewAndSaddleEffect {
        actions = Set.copyOf(actions);
    }

    /** "crews Vehicles and saddles Mounts as though its power were N greater". */
    public PowerBoostForCrewAndSaddleEffect(int powerBonus) {
        this(powerBonus, Action.CREW_AND_SADDLE);
    }

    /** {@code crewOnly}: "crews Vehicles as though its power were N greater". */
    public PowerBoostForCrewAndSaddleEffect(int powerBonus, boolean crewOnly) {
        this(powerBonus, crewOnly ? Set.of(Action.CREW) : Action.CREW_AND_SADDLE);
    }
}
