package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsOpponent;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "259")
@CardRegistration(set = "FCA", collectorNumber = "63")
public class StrixhavenStadium extends Card {

    public StrixhavenStadium() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.COLORLESS),
                        PutCountersEffect.onSelf(CounterType.POINT)),
                "{T}: Add {C}. Put a point counter on this artifact."
        ));

        addTrigger(Triggers.creatureDealsCombatDamageToYou(),
                RemoveCountersEffect.fromSelf(CounterType.POINT, 1));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withEventCondition(new AttackedTargetIsOpponent()),
                SequenceEffect.of(
                        PutCountersEffect.onSelf(CounterType.POINT),
                        new ConditionalEffect(
                                new SourceCounterThreshold(10, CounterType.POINT),
                                SequenceEffect.of(
                                        RemoveCountersEffect.allFromSelf(CounterType.POINT),
                                        new TargetPlayerLosesGameEffect(null)))));
    }
}
