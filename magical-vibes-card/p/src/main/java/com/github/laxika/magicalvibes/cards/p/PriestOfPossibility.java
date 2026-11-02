package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopSevenAndPerpetuallyGainKeywordsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "2")
public class PriestOfPossibility extends Card {

    public PriestOfPossibility() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LookAtTopSevenAndPerpetuallyGainKeywordsEffect());
    }
}
