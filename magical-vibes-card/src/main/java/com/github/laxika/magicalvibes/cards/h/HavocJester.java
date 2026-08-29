package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "M21", collectorNumber = "149")
public class HavocJester extends Card {

    public HavocJester() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED, new DealDamageToAnyTargetEffect(1));
    }
}
