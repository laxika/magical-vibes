package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;

@CardRegistration(set = "NPH", collectorNumber = "121")
public class Spinebiter extends Card {

    public Spinebiter() {
        addEffect(EffectSlot.STATIC, new AssignCombatDamageAsThoughUnblockedEffect());
    }
}
