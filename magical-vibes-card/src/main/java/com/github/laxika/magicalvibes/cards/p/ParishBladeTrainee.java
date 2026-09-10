package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;

@CardRegistration(set = "VOW", collectorNumber = "29")
public class ParishBladeTrainee extends Card {

    public ParishBladeTrainee() {
        addEffect(EffectSlot.ON_DEATH,
                MoveDyingSourceCountersToTargetCreatureEffect.alwaysTriggers(true));
    }
}
