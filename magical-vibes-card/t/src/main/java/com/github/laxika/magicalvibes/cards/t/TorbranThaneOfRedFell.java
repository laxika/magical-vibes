package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "147")
@CardRegistration(set = "ELD", collectorNumber = "367")
@CardRegistration(set = "SLD", collectorNumber = "463")
@CardRegistration(set = "SLD", collectorNumber = "678")
public class TorbranThaneOfRedFell extends Card {

    public TorbranThaneOfRedFell() {
        addStaticEffect(new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(2)
                .fromSources(new PermanentColorInPredicate(Set.of(CardColor.RED))));
    }
}
