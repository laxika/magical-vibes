package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "25")
public class MakindiOx extends Card {

    public MakindiOx() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                        new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
