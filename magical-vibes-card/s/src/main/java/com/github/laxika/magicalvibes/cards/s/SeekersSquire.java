package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "121")
@CardRegistration(set = "2X2", collectorNumber = "87")
public class SeekersSquire extends Card {

    public SeekersSquire() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExploreEffect());
    }
}
