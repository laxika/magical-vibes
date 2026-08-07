package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Returns one specific exiled card to the battlefield under its owner's control.
 *
 * <p>Written into a token blueprint's {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_DEATH}
 * slot for "Return this card to the battlefield under its owner's control when that token dies"
 * (Tatsumasa, the Dragon's Fang). The card is authored with a {@code null} id — the token-creation
 * handler bakes in the id of the card that created the token, which is the card exiled to pay the
 * activation cost. Nothing happens if that card has since left exile.
 *
 * @param exiledCardId the exiled card to return, or {@code null} to be bound at token creation
 */
public record ReturnExiledCardToBattlefieldUnderOwnerControlEffect(UUID exiledCardId) implements CardEffect {
}
