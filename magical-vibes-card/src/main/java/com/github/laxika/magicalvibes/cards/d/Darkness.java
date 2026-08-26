package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "TSB", collectorNumber = "40")
public class Darkness extends Card {

    public Darkness() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombat());
    }
}
