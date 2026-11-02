package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdjustEachCounterKindOnTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "181")
@CardRegistration(set = "AKR", collectorNumber = "210")
public class QuarryHauler extends Card {

    public QuarryHauler() {
        target(TargetFilters.permanent())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new AdjustEachCounterKindOnTargetEffect());
    }
}
