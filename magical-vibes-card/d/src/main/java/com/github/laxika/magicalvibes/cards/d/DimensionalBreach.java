package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SCG", collectorNumber = "9")
public class DimensionalBreach extends Card {

    public DimensionalBreach() {
        addSpellEffect(new ExileAllPermanentsEffect(new PermanentTruePredicate(), true, true));
    }
}
