package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Returns one target card to its controller's hand when it is in either that player's graveyard
 * or their exile zone. The two source zones have independent filters because cards with flashback
 * are restricted only on the exile side of the effect.
 */
public record ReturnTargetCardFromGraveyardOrExileToHandEffect(
        CardPredicate graveyardFilter,
        CardPredicate exileFilter,
        boolean exileOwnedOnly
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.graveyardCards(graveyardFilter, GraveyardSearchScope.CONTROLLERS_GRAVEYARD),
                TargetPredicates.exiledCards(exileFilter)));
    }
}
