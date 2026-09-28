package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;

@CardRegistration(set = "MB2", collectorNumber = "332")
@CardRegistration(set = "MB2", collectorNumber = "569")
public class VuzzleSpaceship extends Card {

    public VuzzleSpaceship() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(6)));

        // Thrusters [6] → Flying.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(6, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));

        // If it would be dealt damage, remove that many +1/+1 counters instead.
        addEffect(EffectSlot.STATIC, new PreventDamageAndRemovePlusOnePlusOneCountersEffect());

        // Lasers [3] → Whenever Vuzzle Spaceship attacks, it deals 1 damage to any target.
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.PLUS_ONE_PLUS_ONE),
                new DealDamageToAnyTargetEffect(1)));
    }
}
