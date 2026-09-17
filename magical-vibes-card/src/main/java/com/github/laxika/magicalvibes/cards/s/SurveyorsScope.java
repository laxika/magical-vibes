package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithAtLeastTwoMoreLandsThanController;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "C13", collectorNumber = "262")
public class SurveyorsScope extends Card {

    public SurveyorsScope() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileSelfCost(),
                        new SearchLibraryEffect(
                                new OpponentsWithAtLeastTwoMoreLandsThanController(),
                                CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD
                        )
                ),
                "{T}, Exile this artifact: Search your library for up to X basic land cards, where X is the number of players who control at least two more lands than you. Put those cards onto the battlefield, then shuffle."
        ));
    }
}
