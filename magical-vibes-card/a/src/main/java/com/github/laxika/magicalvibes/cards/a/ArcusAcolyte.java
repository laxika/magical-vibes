package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "185")
public class ArcusAcolyte extends Card {

    public ArcusAcolyte() {
        ActivatedAbility outlastAbility = new ActivatedAbility(
                true,
                "{G/W}",
                List.of(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                "Outlast {G/W} ({G/W}, {T}: Put a +1/+1 counter on this creature. Outlast only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        );

        addActivatedAbility(outlastAbility);
        addStaticEffect(new GrantActivatedAbilityEffect(
                outlastAbility,
                Subjects.otherCreaturesYouControl(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                        new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE))
                ))
        ));
    }
}
