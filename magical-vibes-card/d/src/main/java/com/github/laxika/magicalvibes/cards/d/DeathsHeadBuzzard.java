package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "63")
@CardRegistration(set = "A25", collectorNumber = "84")
@CardRegistration(set = "VMA", collectorNumber = "115")
public class DeathsHeadBuzzard extends Card {

    public DeathsHeadBuzzard() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new BoostAllCreaturesEffect(-1, -1));
    }
}
