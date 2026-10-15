package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RollDiceAndEnterWithCountersEffect;

@CardRegistration(set = "AFC", collectorNumber = "41")
public class NeverwinterHydra extends Card {

    public NeverwinterHydra() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RollDiceAndEnterWithCountersEffect(new XValue(), 6, CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(4));
    }
}
