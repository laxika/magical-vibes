package com.github.laxika.magicalvibes.model.effect;

public record PutCardFromOpponentGraveyardOntoBattlefieldEffect(boolean tapped) implements CardEffect {
    public PutCardFromOpponentGraveyardOntoBattlefieldEffect() {
        this(false);
    }

    @Override public boolean canTargetGraveyard() { return true; }
}
