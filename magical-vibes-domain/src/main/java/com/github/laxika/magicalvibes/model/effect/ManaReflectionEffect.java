package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if the controller taps a permanent for mana, it produces twice as
 * much of that mana instead. Used by Mana Reflection. Applies to every permanent the controller
 * taps for mana (lands, creatures, artifacts, ...), doubling whatever mana that tap produces.
 * Multiple instances stack multiplicatively (two copies = quadruple), so the effective mana is
 * {@code produced * 2^(number of controlled reflections)}. Applied in the mana-ability resolution
 * via {@code GameQueryService.manaProductionMultiplier}.
 */
public record ManaReflectionEffect() implements CardEffect {
}
