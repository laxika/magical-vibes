package com.github.laxika.magicalvibes.model.effect;

/**
 * Like {@link MayPayManaEffect}, but the player must tap permanents matching
 * {@link TapMultiplePermanentsCost} to get the wrapped effect.
 * Used for "you may tap N untapped creatures you control. If you do, [effect]" patterns.
 */
public record MayPayTapPermanentsEffect(
        TapMultiplePermanentsCost tapCost,
        CardEffect wrapped,
        String prompt
) implements CardEffect {
}
