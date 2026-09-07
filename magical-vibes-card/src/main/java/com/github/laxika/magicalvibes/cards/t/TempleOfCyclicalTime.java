package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;

public class TempleOfCyclicalTime extends Card {

    public TempleOfCyclicalTime() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.BLUE),
                        new RemoveCounterFromSourceEffect(CounterType.TIME, 1)
                ),
                "{T}: Add {U}. Remove a time counter from Temple of Cyclical Time."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(new TransformSelfEffect()),
                "{2}{U}, {T}: Transform Temple of Cyclical Time. Activate only if it has no time counters on it and only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.TIME)),
                "Activate only if Temple of Cyclical Time has no time counters on it."
        ));
    }
}
