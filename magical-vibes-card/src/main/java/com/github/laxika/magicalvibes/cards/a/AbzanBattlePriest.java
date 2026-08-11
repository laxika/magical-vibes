package com.github.laxika.magicalvibes.cards.a;

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

@CardRegistration(set = "KTK", collectorNumber = "1")
public class AbzanBattlePriest extends Card {

    public AbzanBattlePriest() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "Outlast {W} ({W}, {T}: Put a +1/+1 counter on this creature. Outlast only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.LIFELINK,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
        ));
    }
}
