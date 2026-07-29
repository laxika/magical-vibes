package com.github.laxika.magicalvibes.model.effect;

/**
 * ON_OPPONENT_CREATURE_DIES value-materialising effect: the source's controller gains life equal to
 * the dying creature's last-known effective toughness. The toughness is read at trigger time and
 * baked into a concrete {@link GainLifeEffect}, so later changes to the card have no impact.
 * Used by Grim Feast.
 */
public record GainLifeEqualToDyingCreatureToughnessEffect() implements CardEffect {
}
