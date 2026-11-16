package com.github.laxika.magicalvibes.model.condition;

/**
 * The card the ability is about, the triggering card, is currently a card in exile: "if you haven't played it" of an
 * ability that puts a card exiled with a play permission into a graveyard. A card that was played has left exile.
 */
public record TriggeringCardInExile() implements Condition {

    @Override
    public String conditionName() {
        return "triggering card is in exile";
    }

    @Override
    public String conditionNotMetReason() {
        return "triggering card is not in exile";
    }
}
