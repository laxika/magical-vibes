package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may exile a [creature] card from a graveyard": a non-targeting, resolution-time choice over every card in every
 * graveyard that matches {@code filter} ({@code null} for any card). The controller chooses up to one card to exile
 * (one with {@code mandatory}, "exile a card from a graveyard"). Because it targets nothing at stack time it rides on
 * any resolution path, mirroring how {@link ExileUpToNAttackingCreaturesEffect} rides Resounding Silence's cycling
 * ability.
 *
 * <p>It is an EXILE step: the handler asks, resolves itself again with the answer before anything changes, and records
 * the exiled card (and its graveyard's owner), {@code StepOutcome.NONE} when nothing was exiled, so "if you do" is
 * {@code HappenedThisWay.exiled()} and "if a creature card was exiled this way" is
 * {@code HappenedThisWay.toACard(StepKind.EXILE, filter)} (Entrails Feaster, Deathbonnet Hulk).
 */
public record ExileUpToOneCardFromGraveyardEffect(boolean mandatory, CardPredicate filter) implements CardEffect {

    public ExileUpToOneCardFromGraveyardEffect() {
        this(false, null);
    }

    public ExileUpToOneCardFromGraveyardEffect(boolean mandatory) {
        this(mandatory, null);
    }

    /** "You may exile a [filter] card from a graveyard." */
    public static ExileUpToOneCardFromGraveyardEffect matching(CardPredicate filter) {
        return new ExileUpToOneCardFromGraveyardEffect(false, filter);
    }
}
