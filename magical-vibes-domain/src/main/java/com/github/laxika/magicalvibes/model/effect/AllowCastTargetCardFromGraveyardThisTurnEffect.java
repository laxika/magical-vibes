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
 */
public record AllowCastTargetCardFromGraveyardThisTurnEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        boolean exileInsteadOfGraveyard,
        boolean entersTapped
) implements CardEffect {

    public AllowCastTargetCardFromGraveyardThisTurnEffect(CardPredicate filter,
                                                          GraveyardSearchScope scope,
                                                          boolean exileInsteadOfGraveyard) {
        this(filter, scope, exileInsteadOfGraveyard, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter, scope));
    }
}
