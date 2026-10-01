package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

/**
 * Targets a card in a graveyard, then searches its owner's graveyard, hand, and library for any
 * number of cards with the same name and exiles them. Then that player shuffles their library.
 * The graveyard selection can require all matching cards, as it does for Extirpate.
 * <p>
 * Used by: Surgical Extraction, Extirpate, Kotose the Silent Spider
 */
public record ExileTargetGraveyardCardAndSameNameFromZonesEffect(
        GraveyardSearchScope graveyardScope,
        CardPredicate targetFilter,
        boolean trackWithSource,
        boolean exileAllMatchingGraveyardCards) implements CardEffect {

    public ExileTargetGraveyardCardAndSameNameFromZonesEffect() {
        this(false);
    }

    public ExileTargetGraveyardCardAndSameNameFromZonesEffect(boolean exileAllMatchingGraveyardCards) {
        this(GraveyardSearchScope.ALL_GRAVEYARDS,
                new CardNotPredicate(CardPredicateUtils.basicLand()), false, exileAllMatchingGraveyardCards);
    }

    public ExileTargetGraveyardCardAndSameNameFromZonesEffect(
            GraveyardSearchScope graveyardScope, CardPredicate targetFilter, boolean trackWithSource) {
        this(graveyardScope, targetFilter, trackWithSource, false);
    }

    @Override
    public TargetSpec targetSpec() {
        TargetPredicate target = targetFilter == null
                ? TargetPredicates.graveyardCard(graveyardScope)
                : TargetPredicates.graveyardCards(targetFilter, graveyardScope);
        return TargetSpec.benign(target);
    }
}
