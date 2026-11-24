package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "139")
public class MechanizedWarfare extends Card {

    public MechanizedWarfare() {
        addStaticEffect(new AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(1)
                .fromSources(new PermanentAnyOfPredicate(List.of(
                        new PermanentColorInPredicate(Set.of(CardColor.RED)), new PermanentIsArtifactPredicate()))));
    }
}
