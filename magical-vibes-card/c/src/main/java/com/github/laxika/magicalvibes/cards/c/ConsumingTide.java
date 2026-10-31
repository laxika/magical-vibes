package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesNonlandPermanentThenReturnRestEffect;

@CardRegistration(set = "VOW", collectorNumber = "53")
@CardRegistration(set = "DBL", collectorNumber = "320")
public class ConsumingTide extends Card {

    public ConsumingTide() {
        addSpellEffect(new EachPlayerChoosesNonlandPermanentThenReturnRestEffect());
        addSpellEffect(new DrawCardEffect(new OpponentsWithMoreCardsInHandThanController()));
    }
}
