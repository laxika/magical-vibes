package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Cost effect that requires discarding X cards as an additional cost to cast the spell, where X is
 * the value announced for the spell's {@code {X}} (Abandon Hope). Unlike
 * {@link ExileXCardsFromGraveyardCost}, X is <b>not</b> derived from the number of cards paid — the
 * caster announces X first and must then discard exactly that many, so a hand too small to cover
 * the announced X makes the cast illegal (CR 601.2b/601.2h). The spell itself has already left the
 * hand when costs are paid (CR 601.2a), so it can never be discarded to pay for itself.
 *
 * <p>Paid from {@code PlayCardRequest.discardHandCardIndices} (the same wire field escalate uses;
 * no spell carries both costs). Discarding zero cards for X=0 is legal.
 *
 * @param predicate optional restriction every discarded card must match ("discard X land cards" —
 *                  Scorched Earth); {@code null} accepts any card
 * @param label           human-readable description of {@code predicate}, used in rejection messages
 * @param trackManaValue when true, snapshot the total mana value of the discarded cards into the
 *                       spell's resolution {@code xValue}
 */
public record DiscardXCardsCost(CardPredicate predicate, String label, boolean trackManaValue)
        implements HandCardCost {

    public DiscardXCardsCost() {
        this(null, null, false);
    }

    public DiscardXCardsCost(CardPredicate predicate, String label) {
        this(predicate, label, false);
    }

    public DiscardXCardsCost(boolean trackManaValue) {
        this(null, null, trackManaValue);
    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public int requiredCount(int xValue) {
        return xValue;
    }
}
