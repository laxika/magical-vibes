package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLS", collectorNumber = "62")
public class Implode extends Card {

    public Implode() {
        target(TargetFilters.land())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new DrawCardEffect());
    }
}
