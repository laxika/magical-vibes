package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if a source would deal at least {@code threshold} damage to a
 * permanent or player, it deals {@code replacementDamage} instead.
 *
 * <p>The replacement applies to all sources and all damage recipients, including combat damage,
 * and is not prevention. Multiple instances are applied one at a time to the same damage event. "This turn"
 * (Equal Treatment) is this effect floated by {@code GrantStaticEffectToPlayerEffect.untilEndOfTurn}: the floated copy
 * applies to every recipient, whichever player it is on.
 */
public record ReplaceDamageAboveThresholdEffect(int threshold, int replacementDamage) implements CardEffect {
}
