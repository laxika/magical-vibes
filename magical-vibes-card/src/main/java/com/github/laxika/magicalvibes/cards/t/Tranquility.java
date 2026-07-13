package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "259")
@CardRegistration(set = "7ED", collectorNumber = "276")
public class Tranquility extends Card {

    public Tranquility() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsEnchantmentPredicate()));
    }
}
