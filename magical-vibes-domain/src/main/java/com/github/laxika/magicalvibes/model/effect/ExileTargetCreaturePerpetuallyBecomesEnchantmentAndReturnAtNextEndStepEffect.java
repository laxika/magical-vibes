package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles target creature, perpetually changes its card types to enchantment, and schedules its
 * return to the battlefield under the spell controller's control at the next end step.
 */
public record ExileTargetCreaturePerpetuallyBecomesEnchantmentAndReturnAtNextEndStepEffect()
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
