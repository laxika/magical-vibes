package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOC", collectorNumber = "177")
public class IthilienKingfisher extends Card {

    public IthilienKingfisher() {
        // When this creature dies, draw a card.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect());
    }
}
