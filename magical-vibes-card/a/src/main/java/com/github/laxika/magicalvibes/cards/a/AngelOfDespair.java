package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "101")
@CardRegistration(set = "SLD", collectorNumber = "1564")
@CardRegistration(set = "GK2", collectorNumber = "37")
@CardRegistration(set = "UMA", collectorNumber = "196")
@CardRegistration(set = "CMD", collectorNumber = "180")
@CardRegistration(set = "CMA", collectorNumber = "173")
public class AngelOfDespair extends Card {

    public AngelOfDespair() {
        target(TargetFilters.permanent())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DestroyTargetPermanentEffect());
    }
}
