package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "USG", collectorNumber = "266")
public class Hush extends Card {

    public Hush() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsEnchantmentPredicate()));
        addCycling("{2}");
    }
}
