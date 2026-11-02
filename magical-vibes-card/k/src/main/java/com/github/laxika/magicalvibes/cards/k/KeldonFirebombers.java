package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "94")
public class KeldonFirebombers extends Card {

    public KeldonFirebombers() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachPlayerSacrificesDownToCountEffect(3, new PermanentIsLandPredicate()));
    }
}
