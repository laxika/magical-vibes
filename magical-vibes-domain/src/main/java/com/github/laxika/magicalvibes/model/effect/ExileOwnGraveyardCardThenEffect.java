package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * At resolution, optionally exile one matching card from the controller's graveyard. If a card
 * is exiled, the supplied effect is put onto the stack as a reflexive triggered ability.
 *
 * <p>If the follow-up effect requires a graveyard target, that target is chosen only after the
 * exile succeeds, as the reflexive ability goes on the stack.</p>
 *
 * <p>When {@code mandatory} is true, the choice must contain one card whenever a matching card
 * is available.</p>
 *
 * <p>When {@code resolvesInline} is true the follow-up is an "if you do" part of the same ability
 * rather than a "when you do" reflexive trigger, so it resolves immediately after the exile
 * instead of being put onto the stack.</p>
 */
public record ExileOwnGraveyardCardThenEffect(CardPredicate exileFilter, CardEffect thenEffect,
                                               boolean trackWithSource, boolean mandatory,
                                               boolean resolvesInline)
        implements CardEffect {

    public ExileOwnGraveyardCardThenEffect(CardPredicate exileFilter, CardEffect thenEffect,
                                           boolean trackWithSource, boolean mandatory) {
        this(exileFilter, thenEffect, trackWithSource, mandatory, false);
    }

    /** "Exile a card from your graveyard. If you do, [effect]" — the follow-up resolves inline. */
    public static ExileOwnGraveyardCardThenEffect ifYouDo(CardPredicate exileFilter, CardEffect thenEffect,
                                                          boolean trackWithSource) {
        return new ExileOwnGraveyardCardThenEffect(exileFilter, thenEffect, trackWithSource, false, true);
    }

    public ExileOwnGraveyardCardThenEffect(CardPredicate exileFilter, CardEffect thenEffect) {
        this(exileFilter, thenEffect, false, false);
    }

    public ExileOwnGraveyardCardThenEffect(CardPredicate exileFilter, CardEffect thenEffect,
                                           boolean trackWithSource) {
        this(exileFilter, thenEffect, trackWithSource, false);
    }

    /**
     * A reflexive follow-up can itself declare a battlefield target. In that case the target is
     * chosen when this effect's triggered ability is put onto the stack, before the graveyard
     * exile choice resolves.
     */
    @Override
    public TargetSpec targetSpec() {
        // ReturnCardFromGraveyardEffect's target is deliberately selected only after the exile;
        // its targetSpec describes that later graveyard choice, not a target of this wrapper.
        return thenEffect instanceof ReturnCardFromGraveyardEffect
                ? TargetSpec.NONE : thenEffect.targetSpec();
    }
}
