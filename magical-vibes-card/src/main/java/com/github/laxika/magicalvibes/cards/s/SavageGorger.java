package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "M20", collectorNumber = "291")
public class SavageGorger extends Card {

    public SavageGorger() {
        // At the beginning of your end step, if an opponent lost life this turn,
        // put a +1/+1 counter on this creature.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new OpponentLostLifeThisTurn(1),
                new PutCountersOnSourceEffect(1, 1, 1)
        ));
    }
}
