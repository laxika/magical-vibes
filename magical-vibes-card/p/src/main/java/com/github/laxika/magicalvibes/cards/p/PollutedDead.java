package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "116")
public class PollutedDead extends Card {

    public PollutedDead() {
        // When this creature dies, destroy target land.
        target(TargetFilters.land()).addEffect(EffectSlot.ON_DEATH, new DestroyTargetPermanentEffect());
    }
}
