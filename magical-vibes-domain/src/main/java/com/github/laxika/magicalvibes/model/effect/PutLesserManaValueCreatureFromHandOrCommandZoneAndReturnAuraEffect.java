package com.github.laxika.magicalvibes.model.effect;

/**
 * Next of Kin's death trigger, inside the card's "you may" ({@link MayEffect}): put a creature card with lesser mana
 * value than the creature that died from hand or the command zone onto the battlefield, then return the source Aura
 * attached to it at the next end step. The dying creature's mana value is the stack entry's triggering permanent's,
 * read as the ability resolves from its last-known information (CR 608.2h).
 */
public record PutLesserManaValueCreatureFromHandOrCommandZoneAndReturnAuraEffect() implements CardEffect {
}
