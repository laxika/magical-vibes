package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToControlledCreatureCombatDamageEffect;

@CardRegistration(set = "8ED", collectorNumber = "31")
public class NoblePurpose extends Card {

    public NoblePurpose() {
        // Whenever a creature you control deals combat damage, you gain that much life.
        addEffect(EffectSlot.STATIC, new GainLifeEqualToControlledCreatureCombatDamageEffect());
    }
}
