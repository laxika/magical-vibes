package com.github.laxika.magicalvibes.model.effect;

public record ReturnCreatureFromGraveyardToHandEffect() implements CardEffect {
    @Override public boolean canTargetGraveyard() { return true; }
}
