package com.github.laxika.magicalvibes.model.effect;

/**
 * Switches a creature's power and toughness until end of turn.
 * When {@code self} is false the effect targets a creature (e.g. Twisted Image);
 * when {@code self} is true it affects the source permanent with no target
 * (e.g. Turtleshell Changeling's "Switch this creature's power and toughness").
 */
public record SwitchPowerToughnessEffect(boolean self) implements CardEffect {

    /** Targeted variant ("switch target creature's power and toughness"). */
    public SwitchPowerToughnessEffect() {
        this(false);
    }

    @Override
    public boolean canTargetPermanent() {
        return !self;
    }

    @Override
    public boolean isSelfTargeting() {
        return self;
    }
}
