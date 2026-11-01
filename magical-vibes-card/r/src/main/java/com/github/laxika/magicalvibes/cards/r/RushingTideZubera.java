package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SelfWasDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "52")
public class RushingTideZubera extends Card {

    public RushingTideZubera() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ConditionalEffect(
                new SelfWasDealtDamageThisTurn(4), new DrawCardEffect(3)));
    }
}
