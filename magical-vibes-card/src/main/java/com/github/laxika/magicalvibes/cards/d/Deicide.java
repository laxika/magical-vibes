package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndAllWithSameNameFromZonesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "7")
public class Deicide extends Card {

    public Deicide() {
        target(TargetFilters.enchantment());
        addEffect(EffectSlot.SPELL, new ExileTargetPermanentAndAllWithSameNameFromZonesEffect(
                new PermanentIsEnchantmentPredicate(), CardSubtype.GOD, true));
    }
}
