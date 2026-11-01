package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "84")
public class YouthfulScholar extends Card {

    public YouthfulScholar() {
        // When this creature dies, draw two cards.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect(2));
    }
}
