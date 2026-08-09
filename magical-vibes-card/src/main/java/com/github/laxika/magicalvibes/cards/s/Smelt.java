package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M13", collectorNumber = "149")
@CardRegistration(set = "M14", collectorNumber = "156")
@CardRegistration(set = "M19", collectorNumber = "158")
public class Smelt extends Card {

    public Smelt() {
        target(TargetFilters.artifact()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
