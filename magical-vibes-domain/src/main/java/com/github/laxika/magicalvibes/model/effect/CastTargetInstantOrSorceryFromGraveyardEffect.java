package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/**
 * "You may cast target instant or sorcery card from [scope] graveyard[ without paying its mana cost]."
 *
 * <p>{@code exileInsteadOfGraveyard} adds the companion replacement effect "if that spell would be put
 * into a graveyard, exile it instead" (The Dawning Archaic) — it stamps
 * {@code StackEntry.exileInsteadOfGraveyard} on the free cast.</p>
 */
public record CastTargetInstantOrSorceryFromGraveyardEffect(
        GraveyardSearchScope scope,
        boolean withoutPayingManaCost,
        boolean exileInsteadOfGraveyard
) implements CardEffect {

    public CastTargetInstantOrSorceryFromGraveyardEffect(GraveyardSearchScope scope, boolean withoutPayingManaCost) {
        this(scope, withoutPayingManaCost, false);
    }

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.graveyardCard(scope)); }
}
