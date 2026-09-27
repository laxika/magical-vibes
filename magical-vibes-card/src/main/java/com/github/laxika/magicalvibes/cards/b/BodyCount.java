package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreatureDeathsThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "NCC", collectorNumber = "34")
public class BodyCount extends Card {

    public BodyCount() {
        addEffect(EffectSlot.SPELL,
                new DrawCardEffect(new CreatureDeathsThisTurn(CountScope.CONTROLLER)));
    }
}
