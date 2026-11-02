package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "22")
@CardRegistration(set = "DDI", collectorNumber = "2")
@CardRegistration(set = "A25", collectorNumber = "39")
@CardRegistration(set = "EMA", collectorNumber = "37")
@CardRegistration(set = "TSR", collectorNumber = "50")
@CardRegistration(set = "DMR", collectorNumber = "35")
@CardRegistration(set = "C14", collectorNumber = "96")
public class WhitemaneLion extends Card {

    public WhitemaneLion() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ReturnPermanentControlledByPlayerToHandEffect(
                        new PermanentIsCreaturePredicate(), "creature"));
    }
}
