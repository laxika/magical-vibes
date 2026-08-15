package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may cast target instant or sorcery card from [scope] graveyard[ without paying its mana cost]."
 *
 * <p>{@code exileInsteadOfGraveyard} adds the companion replacement effect "if that spell would be put
 * into a graveyard, exile it instead" (The Dawning Archaic) — it stamps
 * {@code StackEntry.exileInsteadOfGraveyard} on the free cast.</p>
 *
 * <p>{@code filter} is an optional additional restriction for cards whose oracle text narrows the
 * instant-or-sorcery choice further, such as an instant-only choice.</p>
 */
public record CastTargetInstantOrSorceryFromGraveyardEffect(
        GraveyardSearchScope scope,
        boolean withoutPayingManaCost,
        boolean exileInsteadOfGraveyard,
        CardPredicate filter
) implements CardEffect {

    public CastTargetInstantOrSorceryFromGraveyardEffect(GraveyardSearchScope scope, boolean withoutPayingManaCost) {
        this(scope, withoutPayingManaCost, false, null);
    }

    public CastTargetInstantOrSorceryFromGraveyardEffect(
            GraveyardSearchScope scope, boolean withoutPayingManaCost, boolean exileInsteadOfGraveyard) {
        this(scope, withoutPayingManaCost, exileInsteadOfGraveyard, null);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.graveyardCard(scope)); }
}
