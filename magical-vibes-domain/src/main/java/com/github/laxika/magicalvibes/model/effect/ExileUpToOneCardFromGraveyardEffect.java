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
 *
 * @param mandatory       "exile a card from a graveyard": exactly one card when any matches
 * @param filter          which cards may be chosen; {@code null} for any card
 * @param trackWithSource the exiled card is tracked with the source permanent, for its abilities that read "cards
 *                        exiled with this creature" (CR 607.2a; the Cemetery cycle: {@link #withSource()})
 */
public record ExileUpToOneCardFromGraveyardEffect(boolean mandatory, CardPredicate filter, boolean trackWithSource)
        implements CardEffect {

    public ExileUpToOneCardFromGraveyardEffect() {
        this(false, null, false);
    }

    public ExileUpToOneCardFromGraveyardEffect(boolean mandatory) {
        this(mandatory, null, false);
    }

    /** "You may exile a [filter] card from a graveyard." */
    public static ExileUpToOneCardFromGraveyardEffect matching(CardPredicate filter) {
        return new ExileUpToOneCardFromGraveyardEffect(false, filter, false);
    }

    /** This exile, tracked with the source permanent ("cards exiled with this creature"). */
    public ExileUpToOneCardFromGraveyardEffect withSource() {
        return new ExileUpToOneCardFromGraveyardEffect(mandatory, filter, true);
    }
}
