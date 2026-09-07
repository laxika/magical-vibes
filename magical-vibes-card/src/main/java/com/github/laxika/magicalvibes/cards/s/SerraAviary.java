package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "HML", collectorNumber = "14")
public class SerraAviary extends Card {

    public SerraAviary() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
