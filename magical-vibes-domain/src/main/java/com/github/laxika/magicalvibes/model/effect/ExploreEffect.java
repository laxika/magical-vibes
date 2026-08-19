package com.github.laxika.magicalvibes.model.effect;

/**
 * A creature explores: reveal the top card of your library.
 * If it's a land card, put it into your hand.
 * Otherwise, put a +1/+1 counter on the exploring creature, then you may put that card
 * into your graveyard (if you don't, it stays on top of your library).
 *
 * <p>The no-argument form applies to the source creature. The targeted form is used by
 * spells such as Enter the Unknown.
 */
public record ExploreEffect(boolean targeted) implements CombatDamageTriggerContextEffect {

    public ExploreEffect() {
        this(false);
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
