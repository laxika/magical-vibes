package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "115")
@CardRegistration(set = "AKR", collectorNumber = "191")
@CardRegistration(set = "MB1", collectorNumber = "115")
public class FeralProwler extends Card {

    public FeralProwler() {
        // When this creature dies, draw a card.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect());
    }
}
