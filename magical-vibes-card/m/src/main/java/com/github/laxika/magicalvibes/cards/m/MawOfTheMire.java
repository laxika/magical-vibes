package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ISD", collectorNumber = "108")
public class MawOfTheMire extends Card {

    public MawOfTheMire() {
        target(TargetFilters.land()).addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new GainLifeEffect(4));
    }
}
