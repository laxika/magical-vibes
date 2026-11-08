package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Dead Man's Chest's "exile cards equal to its power from the top of its owner's library. You may cast spells from
 * among those cards for as long as they remain exiled, and mana of any type can be spent to cast them": exiles
 * {@code count} cards from the top of the library of the stack entry's triggering permanent's owner and lets the
 * ability's controller cast the nonland ones while they stay exiled. "Its power" is {@code count}, the card's
 * {@code TriggeringPermanentPower} (the creature as it last existed on the battlefield, CR 603.10a).
 */
public record ExileTopCardsFromEnchantedCreatureOwnerAndAllowCastEffect(DynamicAmount count) implements CardEffect {
}
