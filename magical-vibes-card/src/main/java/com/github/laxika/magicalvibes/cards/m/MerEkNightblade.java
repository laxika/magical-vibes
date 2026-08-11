package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "79")
public class MerEkNightblade extends Card {

    public MerEkNightblade() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "Outlast {B} ({B}, {T}: Put a +1/+1 counter on this creature. Outlast only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.DEATHTOUCH,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
        ));
    }
}
