package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "52")
public class CryogenRelic extends Card {

    public CryogenRelic() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new DrawCardEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new SacrificeSelfCost(), new PutCounterOnTargetPermanentEffect(CounterType.STUN)),
                "{1}{U}, Sacrifice Cryogen Relic: Put a stun counter on up to one target tapped creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTappedPredicate()
                        )),
                        "Target must be a tapped creature"
                ),
                null,
                null,
                null,
                List.of(),
                0,
                1
        ));
    }
}
