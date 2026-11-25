package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "285")
@CardRegistration(set = "FDN", collectorNumber = "503")
public class TwinbladePaladin extends Card {

    public TwinbladePaladin() {
        addTrigger(Triggers.youGainLife(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        addStaticEffect(new ConditionalEffect(
                new ControllerLifeAtLeast(25),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, Subjects.source())));
    }
}
