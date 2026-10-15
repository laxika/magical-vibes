package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "26")
public class Paraselene extends Card {

    public Paraselene() {
        // Destroy all enchantments. You gain 1 life for each enchantment destroyed this way.
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentIsEnchantmentPredicate(),
                new GainLifeEffect(new EventValue())));
    }
}
