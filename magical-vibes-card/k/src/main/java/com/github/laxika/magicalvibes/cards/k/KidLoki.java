package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentReceivedPlusOnePlusOneCounterThisTurnPredicate;

@CardRegistration(set = "MSH", collectorNumber = "63")
public class KidLoki extends Card {

    public KidLoki() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HEXPROOF,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentReceivedPlusOnePlusOneCounterThisTurnPredicate()));
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
