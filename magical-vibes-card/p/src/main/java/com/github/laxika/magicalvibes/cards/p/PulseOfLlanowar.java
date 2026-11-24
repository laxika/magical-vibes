package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.ReplaceLandManaWithColorEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "INV", collectorNumber = "202")
public class PulseOfLlanowar extends Card {

    public PulseOfLlanowar() {
        addStaticEffect(ReplaceLandManaWithColorEffect.colorOfYourChoice()
                .forLands(new PermanentHasSupertypePredicate(CardSupertype.BASIC)).youControl());
    }
}
