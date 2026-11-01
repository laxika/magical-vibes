package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GS1", collectorNumber = "3")
public class PurpleCrystalCrab extends Card {

    public PurpleCrystalCrab() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect());
    }
}
