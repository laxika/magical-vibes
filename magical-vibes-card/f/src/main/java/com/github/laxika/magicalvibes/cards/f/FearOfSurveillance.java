package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "11")
public class FearOfSurveillance extends Card {

    public FearOfSurveillance() {
        addTrigger(Triggers.attacks(), new SurveilEffect(1));
    }
}
