package com.github.laxika.magicalvibes.model.effect;

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
 */
public record DiscardXCardsCost() implements CostEffect {
}
