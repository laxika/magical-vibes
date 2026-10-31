package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "47")
public class Refocus extends Card {

    public Refocus() {
        target(TargetFilters.creature())
                .addSpellEffect(new UntapPermanentsEffect(TapUntapScope.TARGET));
        addSpellEffect(new DrawCardEffect(1));
    }
}
