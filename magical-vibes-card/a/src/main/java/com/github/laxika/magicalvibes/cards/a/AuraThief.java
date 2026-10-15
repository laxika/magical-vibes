package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "UDS", collectorNumber = "26")
public class AuraThief extends Card {

    public AuraThief() {
        addEffect(EffectSlot.ON_DEATH,
                new GainControlOfAllPermanentsMatchingEffect(new PermanentIsEnchantmentPredicate()));
    }
}
