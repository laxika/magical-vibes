package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "259")
public class StrixhavenStadium extends Card {

    public StrixhavenStadium() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.COLORLESS),
                        new PutCountersOnSelfEffect(CounterType.POINT)),
                "{T}: Add {C}. Put a point counter on this artifact."
        ));

        addEffect(EffectSlot.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU,
                new RemoveCounterFromSourceEffect(CounterType.POINT, 1));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null,
                        SequenceEffect.of(
                                new PutCountersOnSelfEffect(CounterType.POINT),
                                new ConditionalEffect(
                                        new SourceCounterThreshold(10, CounterType.POINT),
                                        SequenceEffect.of(
                                                new RemoveAllCountersEffect(CounterType.POINT),
                                                new TargetPlayerLosesGameEffect(null))))));
    }
}
