package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death-trigger effect: return the dying creature card from its owner's graveyard to the ability
 * controller's hand (fizzles if the card left the graveyard). Used by Enduring Renewal —
 * "Whenever a creature is put into your graveyard from the battlefield, return it to your hand."
 *
 * <p>Card definition uses the no-arg ctor; {@code dyingCardId} is bound at trigger time via
 * {@link DyingCreatureCardAwareEffect} in the ally-creature-dies path.
 *
 * @param dyingCardId the card ID of the dying creature ({@code null} in the card definition)
 */
public record ReturnTriggeringCreatureToOwnerHandEffect(UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnTriggeringCreatureToOwnerHandEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnTriggeringCreatureToOwnerHandEffect(dyingCardId);
    }
}
