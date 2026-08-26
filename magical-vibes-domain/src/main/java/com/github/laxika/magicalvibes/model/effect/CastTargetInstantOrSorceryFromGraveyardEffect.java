package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * "You may cast target instant or sorcery card from [scope] graveyard[ without paying its mana cost]."
 *
 * <p>{@code exileInsteadOfGraveyard} adds the companion replacement effect "if that spell would be put
 * into a graveyard, exile it instead" (The Dawning Archaic) — it stamps
 * {@code StackEntry.exileInsteadOfGraveyard} on the free cast.</p>
 *
 * <p>{@code filter} is an optional additional restriction for cards whose oracle text narrows the
 * instant-or-sorcery choice further, such as an instant-only choice. {@code anyManaType} allows
 * the spell's colored mana requirements to be paid with mana of any type for this cast.</p>
 */
public record CastTargetInstantOrSorceryFromGraveyardEffect(
        GraveyardSearchScope scope,
        boolean withoutPayingManaCost,
        boolean exileInsteadOfGraveyard,
        CardPredicate filter,
        boolean anyManaType
) implements CardEffect {

    public CastTargetInstantOrSorceryFromGraveyardEffect(GraveyardSearchScope scope, boolean withoutPayingManaCost) {
        this(scope, withoutPayingManaCost, false, null, false);
    }

    public CastTargetInstantOrSorceryFromGraveyardEffect(
            GraveyardSearchScope scope, boolean withoutPayingManaCost, boolean exileInsteadOfGraveyard) {
        this(scope, withoutPayingManaCost, exileInsteadOfGraveyard, null, false);
    }

    public CastTargetInstantOrSorceryFromGraveyardEffect(
            GraveyardSearchScope scope, boolean withoutPayingManaCost,
            boolean exileInsteadOfGraveyard, boolean anyManaType) {
        this(scope, withoutPayingManaCost, exileInsteadOfGraveyard, null, anyManaType);
    }

    public CastTargetInstantOrSorceryFromGraveyardEffect(
            GraveyardSearchScope scope, boolean withoutPayingManaCost,
            boolean exileInsteadOfGraveyard, CardPredicate filter) {
        this(scope, withoutPayingManaCost, exileInsteadOfGraveyard, filter, false);
    }

    @Override
    public TargetSpec targetSpec() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        CardPredicate targetFilter = filter == null
                ? instantOrSorcery
                : new CardAllOfPredicate(List.of(instantOrSorcery, filter));
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                targetFilter, scope));
    }
}
