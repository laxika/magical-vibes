package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfEachKindToTargetEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "179")
public class PowerfulBroker extends Card {

    public PowerfulBroker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AddAnotherCounterOfEachKindToTargetEffect()),
                "{T}: For each kind of counter on target permanent or player, give that permanent or player another counter of that kind. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
