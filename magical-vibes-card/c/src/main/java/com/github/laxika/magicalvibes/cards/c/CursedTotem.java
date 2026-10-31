package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "6ED", collectorNumber = "278")
@CardRegistration(set = "MIR", collectorNumber = "299")
@CardRegistration(set = "MH2", collectorNumber = "295")
public class CursedTotem extends Card {

    public CursedTotem() {
        addStaticEffect(new ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect(
                new PermanentIsCreaturePredicate()
        ));
    }
}
