package com.github.laxika.magicalvibes.model.effect;

/**
 * Adds one mana of any type the land untapped to pay this ability's cost could produce (the
 * controller chooses among the available types). Unlike
 * {@link AwardManaOfColorsLandsCouldProduceEffect}, which scans every land in a scope for the
 * colors they could produce, this reads the single land recorded as the untap cost's payment and
 * also offers colorless, because "any type" includes {@code {C}}.
 *
 * <p>If only one type is available it is added automatically; if the untapped land could produce no
 * mana, no mana is produced. Pair with a single-permanent {@link UntapMultiplePermanentsCost}, which
 * records the untapped land on the source permanent (Benthic Explorers).
 */
public record AwardManaOfTypeUntappedLandCouldProduceEffect() implements ManaProducingEffect {
}
