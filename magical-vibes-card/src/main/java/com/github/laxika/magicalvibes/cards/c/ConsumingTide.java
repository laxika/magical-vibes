package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesNonlandPermanentThenReturnRestEffect;

@CardRegistration(set = "VOW", collectorNumber = "53")
public class ConsumingTide extends Card {

    public ConsumingTide() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesNonlandPermanentThenReturnRestEffect());
        addEffect(EffectSlot.SPELL,
                new DrawCardEffect(new OpponentsWithMoreCardsInHandThanController()));
    }
}
