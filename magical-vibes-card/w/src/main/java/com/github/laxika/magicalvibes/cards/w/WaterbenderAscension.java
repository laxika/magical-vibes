package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "79")
public class WaterbenderAscension extends Card {

    public WaterbenderAscension() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(null, SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.QUEST),
                        new ConditionalEffect(new SourceCounterThreshold(4, CounterType.QUEST),
                                new DrawCardEffect(1)))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new WaterbendCost(4), new MakeCreatureUnblockableEffect()),
                "Waterbend {4}: Target creature can't be blocked this turn.",
                TargetFilters.creature()
        ));
    }
}
