package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;

@CardRegistration(set = "MIR", collectorNumber = "229")
public class MindbenderSpores extends Card {

    public MindbenderSpores() {
        addEffect(EffectSlot.ON_BLOCK, new PutCounterOnCombatOpponentEffect(
                CounterType.FUNGUS,
                4,
                new DoesntUntapWithCounterEffect(CounterType.FUNGUS),
                new RemoveCounterFromSourceEffect(CounterType.FUNGUS, 1)));
    }
}
