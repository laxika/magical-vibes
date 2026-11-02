package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class KefkaRulerOfRuin extends Card {

    public KefkaRulerOfRuin() {
        addTrigger(Triggers.opponentLosesLife(),
                new ConditionalEffect(new ControllerTurn(), new DrawCardEffect(new EventValue())));
    }
}
