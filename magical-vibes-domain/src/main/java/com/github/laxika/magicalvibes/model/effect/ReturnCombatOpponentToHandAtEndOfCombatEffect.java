package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: schedule the combat opponent (the creature this permanent blocks, or that becomes
 * blocked by this permanent) to be returned to its owner's hand at end of combat. Kaijin of the
 * Vanishing Touch's "Whenever this creature blocks a creature, return that creature to its owner's
 * hand at end of combat."
 * <p>
 * Placed on {@code Triggers.blocks()} ability (auto-targeting the
 * blocked attacker) and/or on {@code Triggers.becomesBlocked()}
 * with {@code TriggerOptions.EACH_BLOCKER}. The referenced creature
 * is carried as the stack entry's non-targeting target, so the trigger can't fizzle. Resolution
 * queues a {@link com.github.laxika.magicalvibes.model.action.DelayedPermanentAction} of kind
 * {@code RETURN_TO_HAND_AT_END_OF_COMBAT}, so the creature still deals its combat damage and is
 * returned only if it is still on the battlefield then.
 */
public record ReturnCombatOpponentToHandAtEndOfCombatEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
