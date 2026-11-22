package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may cast target [filtered] card from your graveyard this turn" (Jace, Telepath Unbound's
 * −3). Unlike {@link CastTargetInstantOrSorceryFromGraveyardEffect}, which offers the cast right
 * away as the ability resolves, this only grants the permission: the card stays in the graveyard
 * and its controller may cast it any time this turn that they could normally cast it, paying its
 * costs. Turn cleanup drops the permission.
 *
 * <p>{@code exileInsteadOfGraveyard} adds the companion replacement effect "if that spell would be
 * put into your graveyard, exile it instead", registered on the card for the rest of the turn.</p>
 *
 * <p>{@code entersTapped} applies when the granted permission is used for a permanent card: the
 * permanent enters tapped, including when the card is played as a land.</p>
 *
 * <p>{@code cards} names graveyard cards that are no target instead: "choose an instant or sorcery card in your
 * graveyard. You may cast it this turn" is {@link #of} over {@code cardsThisWay(StepKind.CHOOSE)} (Mission
 * Briefing); {@code filter} and {@code scope} are then unused.</p>
 */
public record AllowCastTargetCardFromGraveyardThisTurnEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        boolean exileInsteadOfGraveyard,
        boolean entersTapped,
        CardSubject cards
) implements CardEffect {

    public AllowCastTargetCardFromGraveyardThisTurnEffect {
        if (cards != null && cards.declaresTarget()) {
            throw new IllegalArgumentException("The permission's own spec declares its target: " + cards);
        }
    }

    public AllowCastTargetCardFromGraveyardThisTurnEffect(CardPredicate filter, GraveyardSearchScope scope,
                                                          boolean exileInsteadOfGraveyard, boolean entersTapped) {
        this(filter, scope, exileInsteadOfGraveyard, entersTapped, null);
    }

    /** "You may cast it this turn" of the graveyard cards {@code cards} names, none of them targeted. */
    public static AllowCastTargetCardFromGraveyardThisTurnEffect of(CardSubject cards,
                                                                    boolean exileInsteadOfGraveyard) {
        return new AllowCastTargetCardFromGraveyardThisTurnEffect(null, null, exileInsteadOfGraveyard, false, cards);
    }

    public AllowCastTargetCardFromGraveyardThisTurnEffect(CardPredicate filter,
                                                          GraveyardSearchScope scope,
                                                          boolean exileInsteadOfGraveyard) {
        this(filter, scope, exileInsteadOfGraveyard, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return cards == null ? TargetSpec.benign(TargetPredicates.graveyardCards(filter, scope)) : TargetSpec.NONE;
    }
}
