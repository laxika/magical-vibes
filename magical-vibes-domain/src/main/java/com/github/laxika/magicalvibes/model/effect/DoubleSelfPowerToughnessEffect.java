package com.github.laxika.magicalvibes.model.effect;

/**
 * Doubles the source creature's power and toughness until end of turn.
 * <p>
 * Per CR 701.9a, "To double a creature's power means that creature gets +X/+0,
 * where X is that creature's power as the spell or ability that doubles it resolves."
 * Toughness is handled symmetrically (CR 701.9b).
 */
public record DoubleSelfPowerToughnessEffect() implements CardEffect {

    @Override
    public boolean isSelfTargeting() { return true; }
}
