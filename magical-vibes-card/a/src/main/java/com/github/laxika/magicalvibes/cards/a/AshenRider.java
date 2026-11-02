package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "187")
@CardRegistration(set = "2X2", collectorNumber = "173")
@CardRegistration(set = "PIO", collectorNumber = "203")
@CardRegistration(set = "AFC", collectorNumber = "175")
public class AshenRider extends Card {

    public AshenRider() {
        target(TargetFilters.permanent())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTargetPermanentEffect())
                .addTrigger(Triggers.dies(ObjectRelation.THIS), new ExileTargetPermanentEffect());
    }
}
