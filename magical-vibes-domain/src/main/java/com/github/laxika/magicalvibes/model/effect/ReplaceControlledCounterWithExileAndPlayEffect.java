package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect (Guile): "If a spell or ability you control would counter a spell,
 * instead exile that spell and you may play that card without paying its mana cost."
 *
 * <p>Placed on {@code EffectSlot.STATIC}. Checked in {@code CounterSupport} when any counter effect
 * controlled by this permanent's controller would counter a spell: instead of countering, the spell
 * is exiled and its controller-of-the-counter is offered a free play.
 */
public record ReplaceControlledCounterWithExileAndPlayEffect() implements CardEffect {
}
