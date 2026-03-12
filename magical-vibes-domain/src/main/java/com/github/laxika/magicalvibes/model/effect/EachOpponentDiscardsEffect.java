package com.github.laxika.magicalvibes.model.effect;

public record EachOpponentDiscardsEffect(int amount) implements CardEffect {

    public EachOpponentDiscardsEffect() {
        this(1);
    }
}
