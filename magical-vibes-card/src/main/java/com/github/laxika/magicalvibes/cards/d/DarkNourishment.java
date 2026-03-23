package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetAndGainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "97")
public class DarkNourishment extends Card {

    public DarkNourishment() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetAndGainLifeEffect(3, 3));
    }
}
