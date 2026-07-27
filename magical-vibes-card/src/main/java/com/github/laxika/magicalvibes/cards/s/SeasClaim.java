package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "9ED", collectorNumber = "97")
public class SeasClaim extends Card {

    public SeasClaim() {
        target(TargetFilters.land()).addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesTypeEffect(CardSubtype.ISLAND));
    }
}
