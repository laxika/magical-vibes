package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsAndGainLifePerDestroyedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "26")
public class Paraselene extends Card {

    public Paraselene() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsAndGainLifePerDestroyedEffect(
                new PermanentIsEnchantmentPredicate(), 1));
    }
}
