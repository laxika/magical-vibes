package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "68")
public class HelixPinnacle extends Card {

    public HelixPinnacle() {
        // {X}: Put X tower counters on this enchantment.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(new PutCountersOnSelfEffect(CounterType.TOWER, new XValue())),
                "{X}: Put X tower counters on Helix Pinnacle."
        ));

        // At the beginning of your upkeep, if there are 100 or more tower counters on this
        // enchantment, you win the game.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new SourceCounterThreshold(100, CounterType.TOWER), new WinGameEffect()));
    }
}
