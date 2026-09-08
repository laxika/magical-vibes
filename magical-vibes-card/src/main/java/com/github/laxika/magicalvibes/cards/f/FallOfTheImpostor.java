package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCreatureExileEffect;

@CardRegistration(set = "KHM", collectorNumber = "208")
public class FallOfTheImpostor extends Card {

    public FallOfTheImpostor() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new TargetPlayerChoosesCreatureExileEffect(true));
    }
}
