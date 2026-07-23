package com.github.laxika.magicalvibes.model.condition;

/**
 * A creature dealt damage by the source permanent this turn died (Krovikan Vampire intervening-if).
 * Backed by {@code GameData.sourcesWhoseDamagedCreaturesDiedThisTurn} — set when such a death occurs
 * and kept for the rest of the turn even if the card later leaves the graveyard.
 */
public record SourceDamagedCreatureDiedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a creature dealt damage by this creature this turn died";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature dealt damage by this creature this turn died";
    }
}
