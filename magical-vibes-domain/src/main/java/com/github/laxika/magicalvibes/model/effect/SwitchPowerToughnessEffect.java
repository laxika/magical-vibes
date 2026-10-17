package com.github.laxika.magicalvibes.model.effect;

/**
 * Switches a creature's power and toughness until end of turn. When bound to a multi-target
 * group, the handler applies the switch independently to each target.
 * When {@code self} is false the effect targets a creature (e.g. Twisted Image);
 * when {@code self} is true it affects the source permanent with no target
 * (e.g. Turtleshell Changeling's "Switch this creature's power and toughness").
 * When {@code indefinite} is true the switch has no duration and lasts for the rest of the game
 * (e.g. Mangled Soulrager's boon: "switch its power and toughness").
 */
public record SwitchPowerToughnessEffect(boolean self, boolean indefinite) implements CardEffect {

    /** Until-end-of-turn switch of the given target mode. */
    public SwitchPowerToughnessEffect(boolean self) {
        this(self, false);
    }

    /** Targeted variant ("switch target creature's power and toughness"). */
    public SwitchPowerToughnessEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return self
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.benign(TargetPredicates.creature());
    }
}
