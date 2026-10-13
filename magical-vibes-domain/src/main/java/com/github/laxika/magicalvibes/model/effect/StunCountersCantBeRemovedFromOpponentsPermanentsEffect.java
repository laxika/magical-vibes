package com.github.laxika.magicalvibes.model.effect;

/**
 * STATIC (Fear of Sleep Paralysis): "Stun counters can't be removed from permanents your opponents
 * control." Queried by {@code GameQueryService.cantBecomeUntapped}: a stunned permanent controlled
 * by an opponent of this effect's controller can't lose the stun counter that replaces its untap
 * (CR 122.1d), so it stays tapped and keeps the counter.
 */
public record StunCountersCantBeRemovedFromOpponentsPermanentsEffect() implements CardEffect {
}
