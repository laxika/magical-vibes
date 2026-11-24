package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.LoseAllLandTypesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ICE", collectorNumber = "201")
public class Melting extends Card {

    public Melting() {
        // All lands are no longer snow.
        addStaticEffect(LoseAllLandTypesEffect.supertype(
                CardSupertype.SNOW, Subjects.allPermanents(), new PermanentIsLandPredicate()));
    }
}
