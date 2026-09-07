package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

public class KefkaRulerOfRuin extends Card {

    public KefkaRulerOfRuin() {
        addEffect(EffectSlot.ON_OPPONENT_LOSES_LIFE,
                new ConditionalEffect(new ControllerTurn(), new DrawCardEffect(new EventValue())));
    }
}
