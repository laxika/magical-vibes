package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "43")
public class MindeyeDrake extends Card {

    public MindeyeDrake() {
        // When this creature dies, target player mills five cards.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MillEffect(5, MillRecipient.TARGET_PLAYER));
    }
}
