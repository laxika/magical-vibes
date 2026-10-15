package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "ROE", collectorNumber = "24")
public class HarmlessAssault extends Card {

    public HarmlessAssault() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatByAttackingCreatures());
    }
}
