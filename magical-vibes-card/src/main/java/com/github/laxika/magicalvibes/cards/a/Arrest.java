package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;

@CardRegistration(set = "SOM", collectorNumber = "2")
public class Arrest extends Card {

    public Arrest() {
        addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect());
        addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect());
    }
}
