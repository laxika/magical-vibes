package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect;

@CardRegistration(set = "M11", collectorNumber = "89")
public class Corrupt extends Card {

    public Corrupt() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect(CardSubtype.SWAMP, true));
    }
}
