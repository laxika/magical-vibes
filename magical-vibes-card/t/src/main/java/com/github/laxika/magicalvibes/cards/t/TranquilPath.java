package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "APC", collectorNumber = "89")
public class TranquilPath extends Card {

    public TranquilPath() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsEnchantmentPredicate()));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
