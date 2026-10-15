package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "GRN", collectorNumber = "96")
public class DirectCurrent extends Card {

    public DirectCurrent() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
        addCastingOption(new JumpStartCast());
    }
}
