package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death-trigger effect: return the dying creature card from the ability controller's graveyard to
 * the battlefield under their control (fizzles if the card left that graveyard, or was a token).
 * Battlefield sibling of {@link ReturnTriggeringCreatureToOwnerHandEffect}; used by Angelic Renewal
 * as the payload of a {@link SacrificeSelfThenEffect} — "you may sacrifice this enchantment. If you
 * do, return that card to the battlefield."
 *
 * <p>Card definitions use the no-arg ctor; {@code dyingCardId} is bound at trigger time via
 * {@link DyingCreatureCardAwareEffect} in the ally-creature-dies path.
 *
 * @param dyingCardId the card ID of the dying creature ({@code null} in the card definition)
 */
public record ReturnTriggeringCreatureToBattlefieldEffect(UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnTriggeringCreatureToBattlefieldEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnTriggeringCreatureToBattlefieldEffect(dyingCardId);
    }
}
