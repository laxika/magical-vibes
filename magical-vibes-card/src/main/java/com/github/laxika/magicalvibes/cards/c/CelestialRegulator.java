package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlledCreatureCounterCountAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "174")
public class CelestialRegulator extends Card {

    public CelestialRegulator() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new ControlledCreatureCounterCountAtLeast(1),
                        new SkipNextUntapEffect(TapUntapScope.TARGET)));
    }
}
