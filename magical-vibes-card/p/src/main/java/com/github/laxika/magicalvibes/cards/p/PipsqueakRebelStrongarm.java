package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "TLE", collectorNumber = "82")
public class PipsqueakRebelStrongarm extends Card {

    public PipsqueakRebelStrongarm() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),
                new CantAttackOrBlockAloneEffect(false)
        ));
    }
}
