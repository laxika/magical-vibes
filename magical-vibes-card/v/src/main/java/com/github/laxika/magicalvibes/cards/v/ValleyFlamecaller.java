package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "158")
public class ValleyFlamecaller extends Card {

    public ValleyFlamecaller() {
        addStaticEffect(new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(1)
                .fromSources(new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.LIZARD,
                        CardSubtype.MOUSE,
                        CardSubtype.OTTER,
                        CardSubtype.RACCOON)))
                .toAnyPermanentOrPlayer());
    }
}
