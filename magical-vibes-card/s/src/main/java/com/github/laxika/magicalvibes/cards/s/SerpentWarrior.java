package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "162")
@CardRegistration(set = "8ED", collectorNumber = "161")
@CardRegistration(set = "9ED", collectorNumber = "162")
@CardRegistration(set = "POR", collectorNumber = "109")
@CardRegistration(set = "STH", collectorNumber = "69")
@CardRegistration(set = "TPR", collectorNumber = "117")
@CardRegistration(set = "S99", collectorNumber = "88")
public class SerpentWarrior extends Card {

    public SerpentWarrior() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseLifeEffect(3));
    }
}
