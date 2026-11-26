package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "163")
public class ReaverTitan extends Card {

    public ReaverTitan() {
        addStaticEffect(ProtectionFromManaValueEffect.atMost(3));
        addTrigger(Triggers.attacks(), DealDamageEffect.to(Subjects.eachOpponent(), 5));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(4), AnimatePermanentsEffect.crew()),
                "Crew 4"
        ));
    }
}
