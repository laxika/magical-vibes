package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "POR", collectorNumber = "89")
@CardRegistration(set = "S99", collectorNumber = "76")
@CardRegistration(set = "ME4", collectorNumber = "78")
public class DreadReaper extends Card {

    public DreadReaper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseLifeEffect(5));
    }
}
