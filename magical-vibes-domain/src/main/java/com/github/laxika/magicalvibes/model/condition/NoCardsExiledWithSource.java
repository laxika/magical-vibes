package com.github.laxika.magicalvibes.model.condition;

/**
 * No cards are currently exiled "with" the source permanent (tracked via
 * {@code GameData.exiledCards} / {@code sourcePermanentId}). Used as the intervening-"if" for
 * "if there are no cards exiled with this permanent, …" (Search the City).
 */
public record NoCardsExiledWithSource() implements Condition {

    @Override
    public String conditionName() {
        return "no cards exiled";
    }

    @Override
    public String conditionNotMetReason() {
        return "cards are still exiled with it";
    }
}
