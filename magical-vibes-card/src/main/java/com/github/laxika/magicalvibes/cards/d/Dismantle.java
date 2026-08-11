package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DismantleEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DST", collectorNumber = "57")
public class Dismantle extends Card {

    public Dismantle() {
        target(TargetFilters.artifact()).addEffect(EffectSlot.SPELL, new DismantleEffect());
    }
}
