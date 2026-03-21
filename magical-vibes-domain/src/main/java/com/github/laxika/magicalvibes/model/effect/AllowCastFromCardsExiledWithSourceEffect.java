package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker effect: "You may cast spells from among cards exiled with [this permanent]."
 * While a permanent with this effect is on the battlefield, the controller may cast any
 * card tracked in {@code permanentExiledCards} for the source permanent (paying the mana
 * cost normally). Used by cards like Rona, Disciple of Gix.
 */
public record AllowCastFromCardsExiledWithSourceEffect() implements CardEffect {
}
