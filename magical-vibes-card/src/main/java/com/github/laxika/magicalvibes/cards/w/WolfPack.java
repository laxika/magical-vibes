package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;

@CardRegistration(set = "PTK", collectorNumber = "158")
public class WolfPack extends Card {

    public WolfPack() {
        addEffect(EffectSlot.STATIC, new AssignCombatDamageAsThoughUnblockedEffect());
    }
}
