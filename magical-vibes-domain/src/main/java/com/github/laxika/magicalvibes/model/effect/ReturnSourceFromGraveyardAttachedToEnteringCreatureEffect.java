package com.github.laxika.magicalvibes.model.effect;

/** Returns the source Aura or Equipment from its owner's graveyard and attaches it to "that creature": the stack
 * entry's triggering permanent, read as the ability resolves. */
public record ReturnSourceFromGraveyardAttachedToEnteringCreatureEffect() implements CardEffect {
}
