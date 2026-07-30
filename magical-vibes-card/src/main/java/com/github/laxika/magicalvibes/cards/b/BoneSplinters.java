package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ALA", collectorNumber = "67")
@CardRegistration(set = "AVR", collectorNumber = "88")
public class BoneSplinters extends Card {

    public BoneSplinters() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new SacrificeCreatureCost())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
