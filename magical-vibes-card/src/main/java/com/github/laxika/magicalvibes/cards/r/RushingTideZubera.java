package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SelfWasDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SOK", collectorNumber = "52")
public class RushingTideZubera extends Card {

    public RushingTideZubera() {
        addEffect(EffectSlot.ON_DEATH, new ConditionalEffect(
                new SelfWasDealtDamageThisTurn(4), new DrawCardEffect(3)));
    }
}
