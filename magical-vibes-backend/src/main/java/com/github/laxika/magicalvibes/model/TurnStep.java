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
}
