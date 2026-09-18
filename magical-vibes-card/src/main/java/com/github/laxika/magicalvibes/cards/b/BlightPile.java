package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "82")
public class BlightPile extends Card {

    public BlightPile() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(new LoseLifeEffect(
                        new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasKeywordPredicate(Keyword.DEFENDER))),
                                CountScope.CONTROLLER),
                        LoseLifeRecipient.EACH_OPPONENT)),
                "{2}{B}, {T}: Each opponent loses X life, where X is the number of creatures with defender you control."
        ));
    }
}
