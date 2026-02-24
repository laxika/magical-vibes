package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCreatureOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;

@CardRegistration(set = "SOM", collectorNumber = "113")
public class BluntTheAssault extends Card {

    public BluntTheAssault() {
        addEffect(EffectSlot.SPELL, new GainLifePerCreatureOnBattlefieldEffect());
        addEffect(EffectSlot.SPELL, new PreventAllCombatDamageEffect());
    }
}
