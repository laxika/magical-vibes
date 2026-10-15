package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

public class BlowOffSteam extends Card {

    public BlowOffSteam() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
    }
}
