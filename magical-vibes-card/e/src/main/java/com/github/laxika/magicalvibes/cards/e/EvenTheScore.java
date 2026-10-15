package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.OpponentDrewAtLeastCardsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnColoredCastCostEffect;

@CardRegistration(set = "SNC", collectorNumber = "42")
public class EvenTheScore extends Card {

    public EvenTheScore() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentDrewAtLeastCardsThisTurn(4),
                new ReduceOwnColoredCastCostEffect(ManaColor.BLUE, new Fixed(3))));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new XValue()));
    }
}
