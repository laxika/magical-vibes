package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DescendedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "LCI", collectorNumber = "140")
public class ChildOfTheVolcano extends Card {

    public ChildOfTheVolcano() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new DescendedThisTurn(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
