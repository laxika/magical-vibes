package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "21")
@CardRegistration(set = "8ED", collectorNumber = "23")
@CardRegistration(set = "9ED", collectorNumber = "18")
@CardRegistration(set = "INV", collectorNumber = "20")
public class HolyDay extends Card {

    public HolyDay() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombat());
    }
}
