package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "182")
public class DefendTheCelestus extends Card {

    public DefendTheCelestus() {
        target(TargetFilters.creatureYouControl(), 1, 3)
                .addEffect(EffectSlot.SPELL, DistributeCountersAmongTargetsEffect.chosenAmongTargetCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3)));
    }
}
