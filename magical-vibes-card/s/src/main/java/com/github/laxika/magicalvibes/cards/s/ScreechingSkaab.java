package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "47")
@CardRegistration(set = "ORI", collectorNumber = "70")
@CardRegistration(set = "EMA", collectorNumber = "69")
@CardRegistration(set = "DDQ", collectorNumber = "48")
public class ScreechingSkaab extends Card {

    public ScreechingSkaab() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(2, MillRecipient.CONTROLLER));
    }
}
