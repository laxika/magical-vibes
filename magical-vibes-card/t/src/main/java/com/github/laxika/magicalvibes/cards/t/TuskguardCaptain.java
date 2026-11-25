package com.github.laxika.magicalvibes.cards.t;

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

@CardRegistration(set = "KTK", collectorNumber = "156")
@CardRegistration(set = "2X2", collectorNumber = "163")
@CardRegistration(set = "CMM", collectorNumber = "328")
@CardRegistration(set = "C16", collectorNumber = "173")
@CardRegistration(set = "CM2", collectorNumber = "147")
@CardRegistration(set = "ZNC", collectorNumber = "85")
public class TuskguardCaptain extends Card {

    public TuskguardCaptain() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                "Outlast {G} ({G}, {T}: Put a +1/+1 counter on this creature. Outlast only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addStaticEffect(new GrantKeywordEffect(
                Keyword.TRAMPLE,
                Subjects.creaturesYouControl(),
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
        ));
    }
}
