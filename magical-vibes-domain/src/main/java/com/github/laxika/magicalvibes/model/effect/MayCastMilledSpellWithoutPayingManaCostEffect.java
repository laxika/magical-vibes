package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Marker effect used in the may ability system to indicate that the player may cast the associated
 * instant or sorcery card from a graveyard without paying its mana cost. Queued one per eligible card,
 * accepting one clears the rest: by {@link MillTargetPlayerAndMayCastMilledSpellEffect} and
 * {@link MillControllerAndMayCastMilledSpellEffect} (the cards milled this way) and by
 * {@link MillTargetPlayerAndMayCastSpellFromGraveyardEffect} (any card in the milled opponent's graveyard).
 *
 * @param graveyardScope          whose graveyard the cast card must still be in
 * @param filter                  what else the cast card must be, or {@code null}
 * @param exileInsteadOfGraveyard "if that spell would be put into a graveyard, exile it instead"
 */
public record MayCastMilledSpellWithoutPayingManaCostEffect(
        GraveyardSearchScope graveyardScope,
        CardPredicate filter,
        boolean exileInsteadOfGraveyard
) implements CardEffect {

    public MayCastMilledSpellWithoutPayingManaCostEffect(GraveyardSearchScope graveyardScope, CardPredicate filter) {
        this(graveyardScope, filter, false);
    }

    public MayCastMilledSpellWithoutPayingManaCostEffect() {
        this(GraveyardSearchScope.OPPONENT_GRAVEYARD, null);
    }
}
