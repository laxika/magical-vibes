package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.DistinctCountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "34")
@CardRegistration(set = "FIC", collectorNumber = "111")
public class BlitzballStadium extends Card {

    public BlitzballStadium() {
        targetUpTo(new XValue(), TargetFilters.creature(), Integer.MAX_VALUE)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new MakeCreatureUnblockableEffect(),
                        new GrantEffectToTargetUntilEndOfTurnEffect(
                                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                                new DrawCardEffect(new DistinctCountersOnSource())))
                ),
                "Go for the Goal! — {3}, {T}: Until end of turn, target creature gains "
                        + "\"Whenever this creature deals combat damage to a player, draw a card "
                        + "for each kind of counter on it\" and it can't be blocked this turn.",
                TargetFilters.creature()
        ));
    }
}
