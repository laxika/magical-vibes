package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TurnStep {

    UNTAP("Untap", "Beginning Phase"),
    UPKEEP("Upkeep", "Beginning Phase"),
    DRAW("Draw", "Beginning Phase"),
    PRECOMBAT_MAIN("Precombat Main", "Precombat Main Phase"),
    BEGINNING_OF_COMBAT("Beginning of Combat", "Combat Phase"),
    DECLARE_ATTACKERS("Declare Attackers", "Combat Phase"),
    DECLARE_BLOCKERS("Declare Blockers", "Combat Phase"),
    COMBAT_DAMAGE("Combat Damage", "Combat Phase"),
    END_OF_COMBAT("End of Combat", "Combat Phase"),
    POSTCOMBAT_MAIN("Postcombat Main", "Postcombat Main Phase"),
    END_STEP("End Step", "Ending Phase"),
    CLEANUP("Cleanup", "Ending Phase");

    private final String displayName;
    private final String phaseName;

    public TurnStep next() {
        int nextOrdinal = ordinal() + 1;
        TurnStep[] values = values();
        return nextOrdinal < values.length ? values[nextOrdinal] : null;
    }

    public static TurnStep first() {
        return UNTAP;
    }

    /** True for the five combat steps (used by "activate only during combat" timing restrictions). */
    public boolean isCombatPhase() {
        return this == BEGINNING_OF_COMBAT || this == DECLARE_ATTACKERS || this == DECLARE_BLOCKERS
                || this == COMBAT_DAMAGE || this == END_OF_COMBAT;
    }

    /** True for steps that occur before the combat phase begins (used by "only before combat" timing restrictions). */
    public boolean isBeforeCombat() {
        return ordinal() < BEGINNING_OF_COMBAT.ordinal();
    }

    /** True for steps that occur before the declare attackers step (used by "before attackers are declared" timing restrictions). */
    public boolean isBeforeAttackersDeclared() {
        return ordinal() < DECLARE_ATTACKERS.ordinal();
    }

    /** True for steps that occur before the declare blockers step (used by "before blockers are declared" timing restrictions). */
    public boolean isBeforeBlockersDeclared() {
        return ordinal() < DECLARE_BLOCKERS.ordinal();
    }

    /** True for steps that occur before the combat damage step (used by "only before the combat damage step" timing restrictions). */
    public boolean isBeforeCombatDamage() {
        return ordinal() < COMBAT_DAMAGE.ordinal();
    }

    /** True for steps that occur before the end of combat step (used by "activate only before the end of combat step" timing restrictions). */
    public boolean isBeforeEndOfCombat() {
        return ordinal() < END_OF_COMBAT.ordinal();
    }

    /** True for steps before the ending phase begins. */
    public boolean isBeforeEndStep() {
        return ordinal() < END_STEP.ordinal();
    }
}
