package com.github.laxika.magicalvibes.model.effect;

/**
 * Kaya's Ghostform's "return it to the battlefield under your control": returns the stack entry's triggering card,
 * the card of the enchanted permanent that was put into a graveyard or exiled, from the zone it went to. The
 * enchanted-leaves collector records it, with its graveyard incarnation when it went to a graveyard (CR 400.7): a
 * card that left that zone before the ability resolves stays where it is.
 */
public record ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect() implements CardEffect {
}
