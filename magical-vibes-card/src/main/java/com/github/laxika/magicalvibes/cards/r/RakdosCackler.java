package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.UnleashEffect;

@CardRegistration(set = "RTR", collectorNumber = "220")
public class RakdosCackler extends Card {

    public RakdosCackler() {
        addEffect(EffectSlot.STATIC, new UnleashEffect());
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),
                "it has no +1/+1 counters on it"));
    }
}
