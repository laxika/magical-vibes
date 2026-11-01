package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "121")
@CardRegistration(set = "SLD", collectorNumber = "853")
@CardRegistration(set = "SLD", collectorNumber = "1098")
@CardRegistration(set = "MB2", collectorNumber = "48")
@CardRegistration(set = "FIC", collectorNumber = "287")
@CardRegistration(set = "DSC", collectorNumber = "157")
@CardRegistration(set = "TDC", collectorNumber = "196")
@CardRegistration(set = "M3C", collectorNumber = "204")
public class StitchersSupplier extends Card {

    public StitchersSupplier() {
        // When this creature enters or dies, mill three cards.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(3, MillRecipient.CONTROLLER));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MillEffect(3, MillRecipient.CONTROLLER));
    }
}
