package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "262")
public class NavigationOrb extends Card {

    public NavigationOrb() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect.forCardsMatching(
                                new CardAnyOfPredicate(List.of(
                                        CardPredicateUtils.basicLand(),
                                        new CardSubtypePredicate(CardSubtype.GATE))),
                                "basic land or Gate")
                ),
                "{2}, {T}, Sacrifice this artifact: Search your library for up to two basic land cards "
                        + "and/or Gate cards, reveal those cards, put one onto the battlefield tapped and "
                        + "the other into your hand, then shuffle."
        ));
    }
}
