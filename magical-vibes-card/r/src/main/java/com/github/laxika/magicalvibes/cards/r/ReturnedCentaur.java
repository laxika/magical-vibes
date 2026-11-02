package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "116")
@CardRegistration(set = "THS", collectorNumber = "103")
@CardRegistration(set = "PIO", collectorNumber = "106")
public class ReturnedCentaur extends Card {

    public ReturnedCentaur() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(4, MillRecipient.TARGET_PLAYER));
    }
}
