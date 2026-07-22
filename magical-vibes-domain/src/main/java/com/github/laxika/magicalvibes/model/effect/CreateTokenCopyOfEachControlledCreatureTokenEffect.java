package com.github.laxika.magicalvibes.model.effect;

/**
 * "For each [creature] token you control, create a token that's a copy of that permanent."
 *
 * <p>The set of matching tokens is snapshotted before any copies are made (CR 706.10 style
 * populate-all), so the newly created copies are not themselves copied. Each snapshotted token is
 * copied via all copiable characteristics per CR 707.2, respecting the controller's token
 * multiplier (Doubling Season).
 *
 * <p>{@code creaturesOnly=true} restricts to creature tokens (Rhys the Redeemed).
 * {@code creaturesOnly=false} copies every token you control (Second Harvest).
 */
public record CreateTokenCopyOfEachControlledCreatureTokenEffect(boolean creaturesOnly) implements CardEffect {

    /** Populate-all over creature tokens only (Rhys the Redeemed). */
    public CreateTokenCopyOfEachControlledCreatureTokenEffect() {
        this(true);
    }
}
