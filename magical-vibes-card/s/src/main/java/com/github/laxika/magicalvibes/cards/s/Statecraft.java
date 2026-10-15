package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByCreaturesYouControlEffect;

@CardRegistration(set = "MMQ", collectorNumber = "106")
public class Statecraft extends Card {

    public Statecraft() {
        addEffect(EffectSlot.STATIC, new PreventAllCombatDamageToAndByCreaturesYouControlEffect());
    }
}
