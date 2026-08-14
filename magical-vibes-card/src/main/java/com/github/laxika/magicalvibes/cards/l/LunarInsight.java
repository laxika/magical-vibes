package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DistinctManaValuesAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "FDN", collectorNumber = "46")
public class LunarInsight extends Card {

    public LunarInsight() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new DistinctManaValuesAmongControlledPermanents()));
    }
}
