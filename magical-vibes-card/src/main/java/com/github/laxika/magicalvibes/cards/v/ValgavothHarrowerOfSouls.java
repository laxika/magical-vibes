package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FirstOpponentLifeLossEachTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "6")
public class ValgavothHarrowerOfSouls extends Card {

    public ValgavothHarrowerOfSouls() {
        addEffect(EffectSlot.ON_OPPONENT_LOSES_LIFE,
                new FirstOpponentLifeLossEachTurnTriggerEffect(List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1))));
    }
}
