package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the stack entry's triggering card (the card put into a graveyard that caused the trigger, such as the creature
 * card of a death trigger) from its owner's graveyard, if it is still the same graveyard object when the ability
 * resolves. It is not tracked with the source.
 */
public record ExileTriggeringCardFromGraveyardEffect() implements CardEffect {
}
