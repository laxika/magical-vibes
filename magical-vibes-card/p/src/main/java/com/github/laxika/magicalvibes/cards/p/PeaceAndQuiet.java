package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ULG", collectorNumber = "17")
public class PeaceAndQuiet extends Card {

    public PeaceAndQuiet() {
        target(TargetFilters.enchantment())
                .addSpellEffect(new DestroyTargetPermanentEffect());
        target(TargetFilters.enchantment())
                .addSpellEffect(DestroyTargetPermanentEffect.forTargetGroup(1));
    }
}
