package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "534")
public class BoomerangBladeFlinger extends Card {

    public BoomerangBladeFlinger() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                DealDamageEffect.to(Subjects.eachOpponent(), 1),
                new GainLifeEffect(1)));
    }
}
