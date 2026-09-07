package com.github.laxika.magicalvibes.model.effect;

/**
 * Remembers the targeted player on the source permanent for a later linked ability.
 */
public record RememberTargetPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
