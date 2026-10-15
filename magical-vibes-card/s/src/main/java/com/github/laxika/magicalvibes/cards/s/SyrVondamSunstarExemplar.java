package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "231")
public class SyrVondamSunstarExemplar extends Card {

    public SyrVondamSunstarExemplar() {
        CardEffect counterAndLife = SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new GainLifeEffect(1));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, counterAndLife);
        addEffect(EffectSlot.ON_ALLY_CREATURE_EXILED_FROM_BATTLEFIELD, counterAndLife);

        ConditionalEffect destroyIfPowerAtLeast = new ConditionalEffect(
                new SourcePowerAtLeast(4), new DestroyTargetPermanentEffect());
        target(TargetFilters.nonlandPermanent(), 0, 1)
                .addEffect(EffectSlot.ON_DEATH, destroyIfPowerAtLeast)
                .addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                        new SelfExiledFromBattlefieldEffect(destroyIfPowerAtLeast));
    }
}
