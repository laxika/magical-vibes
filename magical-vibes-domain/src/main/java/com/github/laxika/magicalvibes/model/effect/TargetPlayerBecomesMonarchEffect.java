package com.github.laxika.magicalvibes.model.effect;

/** Makes the targeted player the monarch. */
public record TargetPlayerBecomesMonarchEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
