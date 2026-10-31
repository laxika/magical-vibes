package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RNA", collectorNumber = "110")
public class RubbleReading extends Card {

    public RubbleReading() {
        target(TargetFilters.land()).addSpellEffect(new DestroyTargetPermanentEffect());
        addSpellEffect(new ScryEffect(2));
    }
}
