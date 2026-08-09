package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "UDS", collectorNumber = "114")
public class MultanisDecree extends Card {

    public MultanisDecree() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentIsEnchantmentPredicate(),
                new GainLifeEffect(new Scaled(new EventValue(), 2))));
    }
}
