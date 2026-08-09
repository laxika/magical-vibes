package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimateNonAuraEnchantmentsEffect;

@CardRegistration(set = "UDS", collectorNumber = "13")
public class Opalescence extends Card {

    public Opalescence() {
        addEffect(EffectSlot.STATIC, new AnimateNonAuraEnchantmentsEffect());
    }
}
