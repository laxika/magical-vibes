package com.github.laxika.magicalvibes.model.effect;

/**
 * Adds one mana of any type the land sacrificed to pay this ability's cost could produce (the
 * controller chooses among the available types). Colorless is included because "any type" includes
 * {@code {C}}.
 *
 * <p>If only one type is available it is added automatically; if the sacrificed land could produce
 * no mana, no mana is produced. Pair with a single-permanent {@link SacrificePermanentCost} for a
 * land; the sacrificed land's card is recorded on the source permanent at payment so this effect can
 * still read its mana abilities after the land has left the battlefield (Squandered Resources).
 */
public record AwardManaOfTypeSacrificedLandCouldProduceEffect() implements ManaProducingEffect {
}
