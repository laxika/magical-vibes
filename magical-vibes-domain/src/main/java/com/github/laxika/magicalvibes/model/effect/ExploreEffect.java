package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * A creature explores: reveal the top card of your library.
 * If it's a land card, put it into your hand.
 * Otherwise, put a +1/+1 counter on the exploring creature, then you may put that card
 * into your graveyard (if you don't, it stays on top of your library).
 *
 * <p>The no-argument form applies to the source creature. The targeted form is used by
 * spells such as Enter the Unknown. A reference form applies to a non-targeted permanent
 * identified by the stack entry, such as a permanent returned by the preceding effect. The
 * optional amount repeats the explore process that many times.
 */
public record ExploreEffect(boolean targeted, PermanentReference reference, DynamicAmount amount,
                            boolean replacementApplied)
        implements CombatDamageTriggerContextEffect {

    public ExploreEffect() {
        this(false, null, null, false);
    }

    public ExploreEffect(boolean targeted) {
        this(targeted, null, null, false);
    }

    public ExploreEffect(PermanentReference reference) {
        this(false, reference, null, false);
    }

    public ExploreEffect(boolean targeted, PermanentReference reference) {
        this(targeted, reference, null, false);
    }

    public ExploreEffect(DynamicAmount amount) {
        this(false, null, amount, false);
    }

    public ExploreEffect(boolean targeted, PermanentReference reference, DynamicAmount amount) {
        this(targeted, reference, amount, false);
    }

    public static ExploreEffect afterReplacement(boolean targeted, PermanentReference reference) {
        return new ExploreEffect(targeted, reference, null, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return targeted ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
