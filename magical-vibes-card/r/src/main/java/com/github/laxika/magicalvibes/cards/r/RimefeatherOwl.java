package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToPermanentsWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "42")
public class RimefeatherOwl extends Card {

    public RimefeatherOwl() {
        PermanentCount snowPermanents = new PermanentCount(
                new PermanentHasSupertypePredicate(CardSupertype.SNOW), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(snowPermanents, snowPermanents));
        addEffect(EffectSlot.STATIC, new GrantSupertypeToPermanentsWithCountersEffect(
                CounterType.ICE, CardSupertype.SNOW));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{S}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.ICE)),
                "{1}{S}: Put an ice counter on target permanent.",
                TargetFilters.permanent()
        ));
    }
}
