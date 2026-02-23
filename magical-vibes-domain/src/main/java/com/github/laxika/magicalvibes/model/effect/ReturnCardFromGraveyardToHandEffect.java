package com.github.laxika.magicalvibes.model.effect;

public record ReturnCardFromGraveyardToHandEffect() implements CardEffect {
    @Override public boolean canTargetGraveyard() { return true; }
}
