package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses up to three nonland permanents they do not control, then the targeted
 * opponent chooses one of them. The controller gains permanent control of the rest.
 */
public record ChooseUpToThreeNonlandPermanentsThenOpponentChoosesOneEffect()
        implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
