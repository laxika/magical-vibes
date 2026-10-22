package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers the controller a resolution-time choice of a face-down permanent they control to turn face up.
 *
 * @param creaturesOnly when true only face-down creatures are offered (Hustle // Bustle, Ugin's Mastery);
 *                      when false any face-down permanent is offered (Zimone, Mystery Unraveler).
 */
public record TurnOwnCreatureFaceUpEffect(boolean creaturesOnly) implements CardEffect {

    /** Turn a face-down creature you control face up. */
    public TurnOwnCreatureFaceUpEffect() {
        this(true);
    }

    /** Turn any face-down permanent you control face up. */
    public static TurnOwnCreatureFaceUpEffect anyPermanent() {
        return new TurnOwnCreatureFaceUpEffect(false);
    }
}
