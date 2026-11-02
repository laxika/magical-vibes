package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "195")
public class EliteGuardmage extends Card {

    public EliteGuardmage() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));
    }
}
