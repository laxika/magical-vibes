package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "EVE", collectorNumber = "53")
public class FlameJab extends Card {

    public FlameJab() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
        addCastingOption(new Retrace());
    }
}
