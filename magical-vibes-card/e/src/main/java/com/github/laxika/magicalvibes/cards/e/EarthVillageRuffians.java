package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "219")
public class EarthVillageRuffians extends Card {

    public EarthVillageRuffians() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new EarthbendTargetLandEffect(2));
    }
}
