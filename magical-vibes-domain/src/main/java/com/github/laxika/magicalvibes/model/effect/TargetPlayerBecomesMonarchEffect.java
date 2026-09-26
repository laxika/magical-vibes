package com.github.laxika.magicalvibes.model.effect;

/** Makes the targeted player the monarch. */
public record TargetPlayerBecomesMonarchEffect(int targetGroup) implements CardEffect {

    public TargetPlayerBecomesMonarchEffect() {
        this(-1);
    }

    public static TargetPlayerBecomesMonarchEffect forTargetGroup(int targetGroup) {
        return new TargetPlayerBecomesMonarchEffect(targetGroup);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
