package com.github.laxika.magicalvibes.model.effect;

/** Routes one card selected by Invoke Calamity through its original-zone free-cast path. */
public record CastInvokeCalamityChosenSpellEffect(boolean fromGraveyard) implements CardEffect {
}
