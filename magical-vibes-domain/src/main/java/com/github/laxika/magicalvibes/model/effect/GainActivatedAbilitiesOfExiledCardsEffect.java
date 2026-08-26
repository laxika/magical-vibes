package com.github.laxika.magicalvibes.model.effect;

public record GainActivatedAbilitiesOfExiledCardsEffect(boolean oncePerTurn) implements CardEffect {

    public GainActivatedAbilitiesOfExiledCardsEffect() {
        this(false);
    }
}
