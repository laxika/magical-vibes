package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the owner of the permanent that caused this trigger lose life equal to its mana value (Brainstealer Dragon),
 * read as the effect resolves: the stack entry's triggering permanent as it is now, else as it last existed on the
 * battlefield (CR 608.2h).
 */
public record LoseLifeToOwnerOfTriggeringPermanentEffect() implements CardEffect {
}
