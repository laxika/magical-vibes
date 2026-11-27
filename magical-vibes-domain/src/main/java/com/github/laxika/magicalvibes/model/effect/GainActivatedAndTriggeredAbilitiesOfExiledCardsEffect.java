package com.github.laxika.magicalvibes.model.effect;

/**
 * Static layer-6 effect giving the source all activated and triggered abilities of the card its imprint exiled
 * (Idris, Soul of the TARDIS: "the exiled card", linked to its imprint, CR 607.2a), mana abilities included; a card a
 * borrowed exile ability exiled with it lends nothing.
 */
public record GainActivatedAndTriggeredAbilitiesOfExiledCardsEffect() implements CardEffect {
}
