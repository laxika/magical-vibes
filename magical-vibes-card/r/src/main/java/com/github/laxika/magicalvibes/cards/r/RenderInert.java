package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChosenCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOM", collectorNumber = "123")
public class RenderInert extends Card {

    public RenderInert() {
        target(TargetFilters.permanent())
                .addSpellEffect(new RemoveChosenCountersFromTargetPermanentEffect(5));
        addSpellEffect(new DrawCardEffect(1));
    }
}
