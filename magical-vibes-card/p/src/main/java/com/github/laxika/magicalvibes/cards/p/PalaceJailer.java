package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilOpponentBecomesMonarchEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSR", collectorNumber = "298")
@CardRegistration(set = "MSC", collectorNumber = "140")
@CardRegistration(set = "CMM", collectorNumber = "47")
@CardRegistration(set = "FIC", collectorNumber = "247")
@CardRegistration(set = "LTC", collectorNumber = "174")
public class PalaceJailer extends Card {

    public PalaceJailer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT, new BecomeMonarchEffect());
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new ExileTargetPermanentUntilOpponentBecomesMonarchEffect());
    }
}
