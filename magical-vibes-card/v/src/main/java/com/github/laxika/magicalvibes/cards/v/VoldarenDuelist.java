package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "181")
@CardRegistration(set = "SOI", collectorNumber = "191")
@CardRegistration(set = "GN2", collectorNumber = "44")
public class VoldarenDuelist extends Card {

    public VoldarenDuelist() {
        // Haste is an auto-loaded keyword; no engine wiring needed here.

        // When this creature enters, target creature can't block this turn.
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
