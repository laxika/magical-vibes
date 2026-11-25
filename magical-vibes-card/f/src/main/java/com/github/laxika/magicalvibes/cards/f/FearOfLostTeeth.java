package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "97")
public class FearOfLostTeeth extends Card {

    public FearOfLostTeeth() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                DealDamageEffect.toAnyTarget(1),
                new GainLifeEffect(1)));
    }
}
