package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChosenCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOM", collectorNumber = "123")
public class RenderInert extends Card {

    public RenderInert() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.SPELL, new RemoveChosenCountersFromTargetPermanentEffect(5));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
