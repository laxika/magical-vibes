package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "132")
public class ViridianRevel extends Card {

    public ViridianRevel() {
        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.OPPONENT_OWNS),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
