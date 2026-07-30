package com.github.laxika.magicalvibes.model.effect;

/**
 * Like {@link MayPayManaEffect}, but the player must tap permanents matching
 * {@link TapMultiplePermanentsCost} to get the wrapped effect.
 * Used for "you may tap N untapped creatures you control. If you do, [effect]" patterns.
 *
 * <p>{@code wrapped} may be {@code null} when tapping is the whole point and nothing else happens
 * on payment (Koskun Falls). {@code elseEffect} models the "If you don't, [effect]" half — it
 * resolves instead of {@code wrapped} when the tap is declined or cannot be paid; leave it
 * {@code null} for the plain "if you do" shape, where declining does nothing.
 */
public record MayPayTapPermanentsEffect(
        TapMultiplePermanentsCost tapCost,
        CardEffect wrapped,
        String prompt,
        CardEffect elseEffect
) implements CardEffect {

    public MayPayTapPermanentsEffect(TapMultiplePermanentsCost tapCost, CardEffect wrapped, String prompt) {
        this(tapCost, wrapped, prompt, null);
    }
}
