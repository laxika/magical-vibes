package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayManaAnyNumberOfTimesPutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "194")
public class PrimalAdversary extends Card {

    public PrimalAdversary() {
        AnimatePermanentsEffect animateLands = new AnimatePermanentsEffect(
                3, 3,
                List.of(CardSubtype.WOLF), Set.of(Keyword.HASTE),
                CardColor.GREEN, Set.of(), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN
        );
        targetUpTo(new XValue(), TargetFilters.landYouControl(), 100)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, animateLands);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PayManaAnyNumberOfTimesPutCountersOnSelfEffect("{1}{G}", CounterType.PLUS_ONE_PLUS_ONE,
                        animateLands));
    }
}
