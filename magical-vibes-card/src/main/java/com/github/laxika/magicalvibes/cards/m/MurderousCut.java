package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "81")
public class MurderousCut extends Card {

    public MurderousCut() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DelveCost())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
