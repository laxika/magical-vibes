package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates the delayed triggered ability "When you cast your next instant or sorcery spell this turn, copy that spell X
 * times", X the resolving spell's ({@link CopyNextInstantOrSorceryCastThisTurnEffect#delayedAbility(int)} with that
 * many copies, fixed as it is made); none when X is 0.
 */
public record CopyNextInstantOrSorceryCastThisTurnForXValueEffect() implements CardEffect {
}
