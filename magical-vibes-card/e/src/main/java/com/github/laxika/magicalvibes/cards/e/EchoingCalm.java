package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DST", collectorNumber = "2")
public class EchoingCalm extends Card {

    public EchoingCalm() {
        PermanentIsEnchantmentPredicate enchantmentPredicate = new PermanentIsEnchantmentPredicate();
        target(TargetFilters.enchantment()).addEffect(
                EffectSlot.SPELL,
                new DestroyTargetPermanentAndAllWithSameNameEffect(enchantmentPredicate));
    }
}
