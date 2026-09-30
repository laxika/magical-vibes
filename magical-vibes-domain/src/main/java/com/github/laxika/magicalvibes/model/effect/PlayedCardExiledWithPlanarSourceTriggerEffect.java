package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger marker for playing a card from exile when it was exiled by the current planar source.
 * The follow-up is controlled by the planar source's controller.
 */
public record PlayedCardExiledWithPlanarSourceTriggerEffect(CardEffect followUpEffect)
        implements CardEffect {
}
