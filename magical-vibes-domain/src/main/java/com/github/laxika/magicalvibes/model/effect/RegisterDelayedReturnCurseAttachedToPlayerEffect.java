package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers the delayed return of a Curse to the battlefield attached to the ability's controller at the beginning of
 * the next end step (Lynde, Cheerful Tormentor's "return it to the battlefield attached to you").
 *
 * <p>The Curse is the stack entry's triggering card, as the trigger event left it in a graveyard: the effect reads it as
 * it resolves. A card that left the graveyard and entered it again after the ability triggered is a new object
 * (CR 400.7) and is not returned.
 */
public record RegisterDelayedReturnCurseAttachedToPlayerEffect() implements CardEffect {
}
