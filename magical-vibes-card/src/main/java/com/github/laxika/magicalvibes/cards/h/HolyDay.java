package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;

public class HolyDay extends Card {

    public HolyDay() {
        addEffect(EffectSlot.SPELL, new PreventAllCombatDamageEffect());
    }
}
