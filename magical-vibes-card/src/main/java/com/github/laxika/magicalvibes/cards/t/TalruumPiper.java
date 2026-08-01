package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "VIS", collectorNumber = "98")
public class TalruumPiper extends Card {

    public TalruumPiper() {
        // All creatures with flying able to block this creature do so.
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect(
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
