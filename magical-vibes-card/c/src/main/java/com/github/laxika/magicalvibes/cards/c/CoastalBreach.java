package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "C16", collectorNumber = "6")
public class CoastalBreach extends Card {

    public CoastalBreach() {
        addSpellEffect(ReturnToHandEffect.allPermanentsMatching(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
