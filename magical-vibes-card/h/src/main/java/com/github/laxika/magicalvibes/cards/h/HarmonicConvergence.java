package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutMatchingPermanentsOnTopOfOwnersLibrariesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "ULG", collectorNumber = "103")
public class HarmonicConvergence extends Card {

    public HarmonicConvergence() {
        addEffect(EffectSlot.SPELL,
                new PutMatchingPermanentsOnTopOfOwnersLibrariesEffect(new PermanentIsEnchantmentPredicate()));
    }
}
