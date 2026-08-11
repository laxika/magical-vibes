package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "70")
public class DisownedAncestor extends Card {

    public DisownedAncestor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "Outlast {1}{B} ({1}{B}, {T}: Put a +1/+1 counter on this creature. Outlast only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
