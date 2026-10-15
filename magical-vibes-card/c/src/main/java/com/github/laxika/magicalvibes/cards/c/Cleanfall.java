package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "CHK", collectorNumber = "6")
public class Cleanfall extends Card {

    public Cleanfall() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsEnchantmentPredicate()));
    }
}
