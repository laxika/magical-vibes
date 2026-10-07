package com.github.laxika.magicalvibes.model.effect;

/**
 * Doubles the source creature's power and toughness until end of turn, or only its power when
 * {@code powerOnly} is set (Casey Jones, Asphalt Hooligan).
 * <p>
 * Per CR 701.10b the creature gets +X/+Y where X and Y are its power and toughness as the ability
 * resolves; a negative value is doubled downward rather than clamped (CR 701.10c).
 */
public record DoubleSelfPowerToughnessEffect(boolean powerOnly) implements CardEffect, DoublingEffect {

    public DoubleSelfPowerToughnessEffect() {
        this(false);
    }

    /** Doubles only the source creature's power. */
    public static DoubleSelfPowerToughnessEffect power() {
        return new DoubleSelfPowerToughnessEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
