package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;

@CardRegistration(set = "7ED", collectorNumber = "261")
@CardRegistration(set = "S99", collectorNumber = "139")
public class PrideOfLions extends Card {

    public PrideOfLions() {
        addEffect(EffectSlot.STATIC, new AssignCombatDamageAsThoughUnblockedEffect());
    }
}
