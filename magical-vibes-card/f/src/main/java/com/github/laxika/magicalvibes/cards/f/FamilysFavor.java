package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "59")
@CardRegistration(set = "NCC", collectorNumber = "159")
public class FamilysFavor extends Card {

    public FamilysFavor() {
        target(TargetFilters.attackingCreature())
                .addTrigger(Triggers.youAttack(),
                        new PutCounterOnTargetPermanentEffect(CounterType.SHIELD),
                        new GrantEffectToTargetUntilEndOfTurnEffect(
                                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                                new ConditionalEffect(new SourceCounterThreshold(1, CounterType.SHIELD),
                                        SequenceEffect.of(
                                                new RemoveCounterFromSourceEffect(CounterType.SHIELD, 1),
                                                new DrawCardEffect())))));
    }
}
