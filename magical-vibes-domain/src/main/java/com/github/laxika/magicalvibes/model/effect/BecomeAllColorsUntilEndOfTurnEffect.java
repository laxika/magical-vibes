package com.github.laxika.magicalvibes.model.effect;

/**
 * "This creature becomes all colors until end of turn" (Scrapbasket). A self-scoped effect: on
 * resolution the source permanent becomes white, blue, black, red and green until end of turn
 * (CR 105.3, a layer-5 color-setting effect). Unlike {@link BecomeChosenColorsUntilEndOfTurnEffect}
 * there is no choice and no target — the colors are fixed and applied to the source itself. The
 * handler floats a {@link BecomeChosenColorsUntilEndOfTurnEffect} carrying all five colors so the
 * existing CR 613 layer engine applies it.
 */
public record BecomeAllColorsUntilEndOfTurnEffect() implements CardEffect {
}
