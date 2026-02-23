package com.github.laxika.magicalvibes.model.effect;

public record GainControlOfTargetCreatureUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
