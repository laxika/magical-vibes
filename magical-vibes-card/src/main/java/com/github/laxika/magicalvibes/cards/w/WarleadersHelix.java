package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "DGM", collectorNumber = "116")
public class WarleadersHelix extends Card {

    public WarleadersHelix() {
        // "Warleader's Helix deals 4 damage to any target and you gain 4 life." The fixed 4 life is
        // gained whenever the spell resolves, independent of damage dealt/prevented.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(4));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));
    }
}
