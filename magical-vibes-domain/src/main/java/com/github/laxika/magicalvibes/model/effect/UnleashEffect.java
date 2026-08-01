package com.github.laxika.magicalvibes.model.effect;

/**
 * The as-enters half of unleash (CR 702.98a): "You may have this permanent enter with an
 * additional +1/+1 counter on it."
 * <p>
 * Placed in {@link com.github.laxika.magicalvibes.model.EffectSlot#STATIC} and handled during
 * battlefield entry by {@code BattlefieldEntryService}, which prompts the controller with a "you
 * may" choice; accepting puts one +1/+1 counter on the permanent.
 * <p>
 * The other half of the keyword — "this permanent can't block as long as it has a +1/+1 counter on
 * it" — is not part of this effect: cards pair it with a STATIC
 * {@code CantBlockUnlessEffect(NotCondition(SourceCounterThreshold(1, PLUS_ONE_PLUS_ONE)), …)}.
 */
public record UnleashEffect() implements CardEffect {
}
