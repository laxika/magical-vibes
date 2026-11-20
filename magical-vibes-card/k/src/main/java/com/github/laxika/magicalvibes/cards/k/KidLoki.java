package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentReceivedPlusOnePlusOneCounterThisTurnPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "63")
public class KidLoki extends Card {

    public KidLoki() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.HEXPROOF,
                Subjects.creaturesYouControl(),
                new PermanentReceivedPlusOnePlusOneCounterThisTurnPredicate(true)));
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
