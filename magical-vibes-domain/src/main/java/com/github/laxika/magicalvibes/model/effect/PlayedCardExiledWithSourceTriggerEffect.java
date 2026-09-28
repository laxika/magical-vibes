package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger marker for playing a card that was exiled by the source permanent and is still tracked
 * with that permanent. The optional follow-up effect is resolved by the source permanent's
 * controller when the tracked card is cast from exile or played as a land from exile.
 */
public record PlayedCardExiledWithSourceTriggerEffect(CardEffect followUpEffect) implements CardEffect {

    public PlayedCardExiledWithSourceTriggerEffect() {
        this(new TransformSelfEffect());
    }
}
