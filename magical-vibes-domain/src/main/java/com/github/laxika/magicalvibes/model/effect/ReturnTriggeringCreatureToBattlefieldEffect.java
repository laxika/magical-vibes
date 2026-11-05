package com.github.laxika.magicalvibes.model.effect;

/**
 * Death-trigger effect: return the dying creature card from the ability controller's graveyard to
 * the battlefield under their control (fizzles if the card left that graveyard, or was a token).
 * Battlefield sibling of {@link ReturnTriggeringCreatureToOwnerHandEffect}; used by Angelic Renewal
 * as the payload of a {@link SacrificeSelfThenEffect} — "you may sacrifice this enchantment. If you
 * do, return that card to the battlefield."
 *
 * <p>The card that died is the stack entry's triggering card.
 */
public record ReturnTriggeringCreatureToBattlefieldEffect() implements CardEffect {
}
