package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "19")
public class HeartOfLight extends Card {

    public HeartOfLight() {
        setNeedsTarget(true);
        addEffect(EffectSlot.STATIC, new PreventAllDamageToAndByEnchantedCreatureEffect());
    }
}
