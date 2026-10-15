package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;

@CardRegistration(set = "SCG", collectorNumber = "7")
public class DawnElemental extends Card {

    public DawnElemental() {
        addEffect(EffectSlot.STATIC, new PreventAllDamageEffect());
    }
}
