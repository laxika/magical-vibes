package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "90")
@CardRegistration(set = "MKC", collectorNumber = "144")
public class WhisperingSnitch extends Card {

    public WhisperingSnitch() {
        addTrigger(Triggers.youSurveil().firstTimeEachTurn(),
                SequenceEffect.of(
                        DealDamageEffect.to(Subjects.eachOpponent(), 1),
                        new GainLifeEffect(1)));
    }
}
