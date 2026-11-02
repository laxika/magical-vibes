package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "6")
public class CivicStalwart extends Card {

    public CivicStalwart() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostAllOwnCreaturesEffect(1, 1));
    }
}
