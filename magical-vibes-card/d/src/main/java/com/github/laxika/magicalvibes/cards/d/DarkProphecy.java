package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M14", collectorNumber = "93")
public class DarkProphecy extends Card {

    public DarkProphecy() {
        // Whenever a creature you control dies, you draw a card and you lose 1 life.
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), new DrawCardEffect(1),
                new LoseLifeEffect(1));
    }
}
