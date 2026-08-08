package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may cast target [filter] card from [scope] graveyard" — the card is cast for its normal costs,
 * so the permission is recorded against that specific card rather than the spell being put on the
 * stack during resolution.
 *
 * <p>{@code exileInsteadOfGraveyard} adds the companion replacement "if that spell would be put into
 * a graveyard, exile it instead" (Toshiro Umezawa).</p>
 */
public record GrantTargetGraveyardCardCastEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        boolean exileInsteadOfGraveyard
) implements CardEffect {

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.graveyardCard(scope)); }
}
