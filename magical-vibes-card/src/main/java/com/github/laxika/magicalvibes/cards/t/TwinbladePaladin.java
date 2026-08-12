package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "M20", collectorNumber = "285")
public class TwinbladePaladin extends Card {

    public TwinbladePaladin() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerLifeAtLeast(25),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF)));
    }
}
