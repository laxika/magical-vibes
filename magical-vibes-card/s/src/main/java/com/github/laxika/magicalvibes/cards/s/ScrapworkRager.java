package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "123")
public class ScrapworkRager extends Card {

    public ScrapworkRager() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(), new LoseLifeEffect(1));

        addUnearth("{3}{B}");
    }
}
