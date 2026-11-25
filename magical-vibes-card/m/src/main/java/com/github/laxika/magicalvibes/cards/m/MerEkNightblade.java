package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "79")
@CardRegistration(set = "IMA", collectorNumber = "97")
public class MerEkNightblade extends Card {

    public MerEkNightblade() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                "Outlast {B} ({B}, {T}: Put a +1/+1 counter on this creature. Outlast only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addStaticEffect(new GrantKeywordEffect(
                Keyword.DEATHTOUCH,
                Subjects.creaturesYouControl(),
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
        ));
    }
}
