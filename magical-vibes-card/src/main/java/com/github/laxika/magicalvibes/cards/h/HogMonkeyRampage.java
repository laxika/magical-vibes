package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLE", collectorNumber = "255")
public class HogMonkeyRampage extends Card {

    public HogMonkeyRampage() {
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.SPELL,
                PutCounterOnTargetPermanentEffect.withResolutionCondition(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentPowerAtLeastPredicate(4)));

        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new FightTargetsEffect());
    }
}
