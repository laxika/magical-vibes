package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "73")
@CardRegistration(set = "SIS", collectorNumber = "21")
public class SelhoffOccultist extends Card {

    public SelhoffOccultist() {
        addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER),
                new MillEffect(1, MillRecipient.TARGET_PLAYER));
    }
}
