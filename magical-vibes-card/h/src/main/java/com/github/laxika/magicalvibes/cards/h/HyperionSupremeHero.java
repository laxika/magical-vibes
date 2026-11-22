package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PreventAllButOneDamageToControllerAndPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "599")
public class HyperionSupremeHero extends Card {

    public HyperionSupremeHero() {
        addStaticEffect(new PreventAllButOneDamageToControllerAndPermanentsEffect(
                new PermanentHasSubtypePredicate(CardSubtype.HERO)));
    }
}
