package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "93")
public class Throttle extends Card {

    public Throttle() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-4, -4));
    }
}
