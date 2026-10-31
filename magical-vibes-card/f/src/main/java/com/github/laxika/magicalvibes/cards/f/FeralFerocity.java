package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "560")
public class FeralFerocity extends Card {

    public FeralFerocity() {
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(4, 4));
    }
}
