package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "182")
@CardRegistration(set = "M11", collectorNumber = "173")
public class Fog extends Card {

    public Fog() {
        addEffect(EffectSlot.SPELL, new PreventAllCombatDamageEffect());
    }
}
