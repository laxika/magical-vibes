package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that turns every land on the battlefield (all players') into a creature with the
 * given fixed power and toughness while still being a land (Nature's Revolt). The layer-4 type
 * change is applied by the layered pass; the base P/T and creature-ness are filled in the
 * accumulator pass by the matching handler, and combat/targeting queries recognise animated lands
 * via {@code GameQueryService.hasAnimateLandEffect}.
 */
public record AllLandsAreCreaturesEffect(int power, int toughness) implements CardEffect {
}
