package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * "Return any number of other creature cards with total power X or less from your graveyard to the
 * battlefield, where X is this creature's power." Nothing is targeted: the controller chooses the cards as
 * the ability resolves. X is the dying creature's last known power, carried on the stack entry's event
 * value by the death-trigger collector. The dying card itself is excluded once it is bound with
 * {@link #boundToDyingCard}.
 *
 * @param dyingCardId the card that died, which "other" excludes; {@code null} until bound
 */
public record ReturnOtherCreatureCardsWithTotalPowerFromGraveyardToBattlefieldEffect(UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnOtherCreatureCardsWithTotalPowerFromGraveyardToBattlefieldEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnOtherCreatureCardsWithTotalPowerFromGraveyardToBattlefieldEffect(dyingCardId);
    }
}
