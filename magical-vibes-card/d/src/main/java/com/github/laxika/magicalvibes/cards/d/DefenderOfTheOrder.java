package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "11")
public class DefenderOfTheOrder extends Card {

    public DefenderOfTheOrder() {
        addMorph("{W}{W}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), BoostEffect.creaturesYouControl(0, 2));
    }
}
