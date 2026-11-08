package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule the targeted permanent for destruction at end of combat. "Destroy it at end of combat"
 * riders on a spell or activated ability that also does something to that same target (e.g. Goblin
 * Sappers' "Target creature you control can't be blocked this turn. Destroy it at end of combat.").
 * <p>
 * Unlike {@link DestroyCombatOpponentAtEndOfCombatEffect} this is a real target chosen on
 * activation, and unlike {@link DestroySelfAtEndOfCombatEffect} the scheduled permanent is the
 * target rather than the source. At resolution a delayed
 * {@link com.github.laxika.magicalvibes.model.action.DelayedPermanentAction} is queued for the
 * target; it is drained in {@code CombatService.processEndOfCombatDestructions()} so regeneration
 * and indestructible apply.
 *
 * <p>{@link #ofTriggeringPermanent()} schedules the stack entry's triggering permanent instead: "whenever this
 * creature deals combat damage to a creature, destroy that creature at end of combat" (the basilisks, Sosuke, Son of
 * Seshiro); it then targets nothing.
 *
 * @param cannotBeRegenerated whether the scheduled destruction ignores regeneration shields
 * @param triggeringPermanent whether the scheduled permanent is the stack entry's triggering permanent
 */
public record DestroyTargetPermanentAtEndOfCombatEffect(boolean cannotBeRegenerated, boolean triggeringPermanent)
        implements CardEffect, RemovalEffect {

    public DestroyTargetPermanentAtEndOfCombatEffect() {
        this(false);
    }

    public DestroyTargetPermanentAtEndOfCombatEffect(boolean cannotBeRegenerated) {
        this(cannotBeRegenerated, false);
    }

    /** "Destroy that creature at end of combat": the stack entry's triggering permanent. */
    public static DestroyTargetPermanentAtEndOfCombatEffect ofTriggeringPermanent() {
        return new DestroyTargetPermanentAtEndOfCombatEffect(false, true);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }

    @Override
    public TargetSpec targetSpec() {
        if (triggeringPermanent) {
            return TargetSpec.NONE;
        }
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
