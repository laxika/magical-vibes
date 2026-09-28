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
 * (Mavinda, Students' Advocate). {@code additionalGraveyardExileCount} and {@code escape} support
 * targeted escape permissions such as Desdemona, Freedom's Edge.</p>
 */
public record GrantTargetGraveyardCardCastEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        boolean exileInsteadOfGraveyard,
        int additionalGenericCost,
        boolean anyManaType,
        int additionalGraveyardExileCount,
        boolean escape
) implements CardEffect {

    public GrantTargetGraveyardCardCastEffect {
        if (additionalGraveyardExileCount < 0) {
            throw new IllegalArgumentException("Additional graveyard exile count cannot be negative");
        }
    }

    public GrantTargetGraveyardCardCastEffect(
            CardPredicate filter, GraveyardSearchScope scope, boolean exileInsteadOfGraveyard) {
        this(filter, scope, exileInsteadOfGraveyard, 0, false, 0, false);
    }

    public GrantTargetGraveyardCardCastEffect(
            CardPredicate filter, GraveyardSearchScope scope, boolean exileInsteadOfGraveyard,
            int additionalGenericCost) {
        this(filter, scope, exileInsteadOfGraveyard, additionalGenericCost, false, 0, false);
    }

    public GrantTargetGraveyardCardCastEffect(
            CardPredicate filter, GraveyardSearchScope scope, boolean exileInsteadOfGraveyard,
            boolean anyManaType) {
        this(filter, scope, exileInsteadOfGraveyard, 0, anyManaType, 0, false);
    }

    public GrantTargetGraveyardCardCastEffect(
            CardPredicate filter, GraveyardSearchScope scope, boolean exileInsteadOfGraveyard,
            int additionalGenericCost, boolean anyManaType) {
        this(filter, scope, exileInsteadOfGraveyard, additionalGenericCost, anyManaType, 0, false);
    }

    public static GrantTargetGraveyardCardCastEffect withEscape(
            CardPredicate filter, GraveyardSearchScope scope, int additionalGraveyardExileCount) {
        return new GrantTargetGraveyardCardCastEffect(
                filter, scope, false, 0, false, additionalGraveyardExileCount, true);
    }

    @Override public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter, scope));
    }
}
