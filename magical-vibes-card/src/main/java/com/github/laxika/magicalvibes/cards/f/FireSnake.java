package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "POR", collectorNumber = "127")
public class FireSnake extends Card {

    public FireSnake() {
        // When this creature dies, destroy target land.
        target(TargetFilters.land()).addEffect(EffectSlot.ON_DEATH, new DestroyTargetPermanentEffect());
    }
}
