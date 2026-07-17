package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "97")
public class DarkNourishment extends Card {

    public DarkNourishment() {
        // "Dark Nourishment deals 3 damage to any target and you gain 3 life." The fixed 3 life is
        // gained whenever the spell resolves, independent of damage dealt/prevented.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
