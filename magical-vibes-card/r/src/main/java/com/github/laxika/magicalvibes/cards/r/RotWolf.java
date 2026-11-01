package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "90")
@CardRegistration(set = "TD2", collectorNumber = "54")
public class RotWolf extends Card {

    public RotWolf() {
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
