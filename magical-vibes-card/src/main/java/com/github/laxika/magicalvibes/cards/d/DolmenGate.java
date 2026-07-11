package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToAttackingCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "256")
public class DolmenGate extends Card {

    public DolmenGate() {
        // "Prevent all combat damage that would be dealt to attacking creatures you control."
        addEffect(EffectSlot.STATIC, new PreventCombatDamageToAttackingCreaturesYouControlEffect());
    }
}
