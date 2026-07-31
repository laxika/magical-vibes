package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller was dealt damage this turn by a red instant or sorcery spell (Suffocation's
 * "Cast this spell only if you were dealt damage this turn by a red instant or sorcery spell").
 * Reads {@code GameData.lastRedSpellDamagerThisTurn}, which is cleared at turn cleanup.
 */
public record DealtDamageByRedSpellThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "you were dealt damage this turn by a red instant or sorcery spell";
    }

    @Override
    public String conditionNotMetReason() {
        return "you were not dealt damage this turn by a red instant or sorcery spell";
    }
}
