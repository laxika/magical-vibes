package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLS", collectorNumber = "1")
public class AuraBlast extends Card {

    public AuraBlast() {
        target(TargetFilters.enchantment())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new DrawCardEffect(1));
    }
}
