package com.github.laxika.magicalvibes.model.effect;

/** Returns the source card from exile to the battlefield under its owner's control. */
public record ReturnSourceCardFromExileToBattlefieldEffect(boolean tapped, boolean attacking) implements CardEffect {

    public ReturnSourceCardFromExileToBattlefieldEffect(boolean tapped) {
        this(tapped, false);
    }
}
