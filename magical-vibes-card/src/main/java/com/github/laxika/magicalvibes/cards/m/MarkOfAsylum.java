package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventNoncombatDamageToCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CON", collectorNumber = "10")
public class MarkOfAsylum extends Card {

    public MarkOfAsylum() {
        // "Prevent all noncombat damage that would be dealt to creatures you control."
        addEffect(EffectSlot.STATIC, new PreventNoncombatDamageToCreaturesYouControlEffect());
    }
}
