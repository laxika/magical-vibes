package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may cast target [filter] card from [scope] graveyard" — the card is cast for its normal costs,
 * so the permission is recorded against that specific card rather than the spell being put on the
 * stack during resolution.
 *
 * <p>{@code exileInsteadOfGraveyard} adds the companion replacement "if that spell would be put into
 * a graveyard, exile it instead" (Toshiro Umezawa). {@code additionalGenericCost} adds a
 * conditional generic cost when the spell does not target a creature controlled by its caster
 * (Mavinda, Students' Advocate).</p>
 */
public record GrantTargetGraveyardCardCastEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        boolean exileInsteadOfGraveyard,
        int additionalGenericCost,
        boolean anyManaType
) implements CardEffect {

    public GrantTargetGraveyardCardCastEffect(
            CardPredicate filter, GraveyardSearchScope scope, boolean exileInsteadOfGraveyard) {
        this(filter, scope, exileInsteadOfGraveyard, 0, false);
    }

    public GrantTargetGraveyardCardCastEffect(
            CardPredicate filter, GraveyardSearchScope scope, boolean exileInsteadOfGraveyard,
            int additionalGenericCost) {
        this(filter, scope, exileInsteadOfGraveyard, additionalGenericCost, false);
    }

    public GrantTargetGraveyardCardCastEffect(
            CardPredicate filter, GraveyardSearchScope scope, boolean exileInsteadOfGraveyard,
            boolean anyManaType) {
        this(filter, scope, exileInsteadOfGraveyard, 0, anyManaType);
    }

    @Override public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter, scope));
    }
}
