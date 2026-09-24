package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "HOC", collectorNumber = "15")
@CardRegistration(set = "HOC", collectorNumber = "55")
public class TheGaffer extends Card {

    public TheGaffer() {
        // At the beginning of each end step, if you gained 3 or more life this turn, draw a card.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new GainedLifeThisTurn(3),
                new DrawCardEffect(1)));
    }
}
