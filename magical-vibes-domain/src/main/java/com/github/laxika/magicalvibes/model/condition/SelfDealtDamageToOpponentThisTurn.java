package com.github.laxika.magicalvibes.model.condition;

/**
 * The source permanent dealt damage to an opponent of its current controller this turn (Whirling
 * Dervish). Backed by the per-source combat-damage-to-players tracking — combat is the only in-engine
 * path through which such a creature deals damage to a player. Checks against the source's current
 * controller, so a creature that changed control after dealing the damage does not qualify.
 */
public record SelfDealtDamageToOpponentThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "dealt damage to an opponent this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it dealt no damage to an opponent this turn";
    }
}
