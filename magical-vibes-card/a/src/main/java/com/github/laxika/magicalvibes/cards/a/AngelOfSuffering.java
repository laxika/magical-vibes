package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerAndMillEffect;

@CardRegistration(set = "SNC", collectorNumber = "67")
public class AngelOfSuffering extends Card {

    public AngelOfSuffering() {
        addEffect(EffectSlot.STATIC, new PreventAllDamageToControllerAndMillEffect());
    }
}
