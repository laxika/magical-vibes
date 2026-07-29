package com.github.laxika.magicalvibes.model.effect;

/**
 * "Becomes colorless until end of turn" (Raging Spirit, Ersatz Gnomes). Layer-5 color-setting
 * effect with an empty replacement set (CR 105.3): the permanent's colors are replaced by no
 * colors until end of turn.
 *
 * <p>With {@code targeted = false} the effect is self-scoped ("this creature becomes colorless",
 * Raging Spirit); with {@code targeted = true} it applies to the chosen permanent instead
 * ("target permanent becomes colorless until end of turn", Ersatz Gnomes).
 *
 * <p>The activated-ability instance is resolved by its handler, which floats a second instance
 * anchored on the affected permanent; the CR 613 layer engine applies that floating instance (see
 * {@code LayerSystemService.applyL5Instance}). The until-end-of-turn sibling of the static
 * {@link BecomeColorlessEffect}.
 */
public record BecomeColorlessUntilEndOfTurnEffect(boolean targeted) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return targeted ? TargetSpec.benign(TargetCategory.PERMANENT) : TargetSpec.NONE;
    }
}
