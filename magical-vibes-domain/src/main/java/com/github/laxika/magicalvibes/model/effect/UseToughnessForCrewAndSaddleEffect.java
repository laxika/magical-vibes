package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;

/**
 * Uses the source creature's effective toughness instead of its power for the named keyword
 * actions: "this creature crews Vehicles [and saddles Mounts] using its toughness rather than its
 * power".
 *
 * @param actions the keyword actions it applies to: crew only (Giant Ox), crew and saddle
 *                (Interface Ace)
 */
public record UseToughnessForCrewAndSaddleEffect(Set<Action> actions) implements CrewAndSaddlePowerModifierEffect {

    public UseToughnessForCrewAndSaddleEffect {
        actions = Set.copyOf(actions);
    }

    /** "crews Vehicles and saddles Mounts using its toughness". */
    public UseToughnessForCrewAndSaddleEffect() {
        this(Action.CREW_AND_SADDLE);
    }

    /** {@code crewOnly}: "crews Vehicles using its toughness". */
    public UseToughnessForCrewAndSaddleEffect(boolean crewOnly) {
        this(crewOnly ? Set.of(Action.CREW) : Action.CREW_AND_SADDLE);
    }

    @Override
    public int powerBonus() {
        return 0;
    }

    @Override
    public boolean usesToughnessInsteadOfPower() {
        return true;
    }
}
