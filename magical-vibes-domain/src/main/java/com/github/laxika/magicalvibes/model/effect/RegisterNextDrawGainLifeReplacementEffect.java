package com.github.laxika.magicalvibes.model.effect;

/**
 * Words of Worship's activated ability: registers a one-shot, turn-scoped replacement of the
 * controller's next draw with gaining 5 life. Repeated activations replace successive draws.
 */
public record RegisterNextDrawGainLifeReplacementEffect() implements CardEffect {
}
