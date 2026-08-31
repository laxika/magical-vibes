package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;

@CardRegistration(set = "LEG", collectorNumber = "12")
public class EnchantedBeing extends Card {

    public EnchantedBeing() {
        addEffect(EffectSlot.STATIC, new PreventDamageToSelfFromCreaturesEffect(
                new PermanentIsEnchantedPredicate(), true));
    }
}
