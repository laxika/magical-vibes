package com.github.laxika.magicalvibes.model.effect;

/**
 * Each targeted player loses life and sacrifices a creature of their choice.
 * The sacrifice choices are made in APNAP order after the life loss is applied.
 */
public record EachTargetPlayerLosesLifeAndSacrificesCreatureEffect(int lifeLoss) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
