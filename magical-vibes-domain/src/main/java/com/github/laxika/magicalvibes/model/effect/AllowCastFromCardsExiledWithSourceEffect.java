package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker effect: "You may cast spells from among cards exiled with [this permanent]."
 * While a permanent with this effect is on the battlefield, the controller may cast any
 * card tracked in {@code permanentExiledCards} for the source permanent.
 *
 * @param anyManaType if true, mana of any type can be spent to cast the exiled spell
 *                    (e.g. Hostage Taker). If false, normal mana costs apply (e.g. Rona).
 */
public record AllowCastFromCardsExiledWithSourceEffect(boolean anyManaType) implements CardEffect {
}
