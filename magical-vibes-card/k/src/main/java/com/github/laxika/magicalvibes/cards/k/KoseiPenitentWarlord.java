package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEC", collectorNumber = "26")
@CardRegistration(set = "NEC", collectorNumber = "67")
public class KoseiPenitentWarlord extends Card {

    public KoseiPenitentWarlord() {
        addStaticEffect(new ConditionalEffect(
                new AllOf(List.of(
                        new Enchanted(),
                        new Equipped(),
                        new SourceCounterThreshold(1, CounterType.ANY))),
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                        SequenceEffect.of(
                                new DrawCardEffect(new EventValue()),
                                new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OTHER_OPPONENT))),
                        GrantScope.SELF)));
    }
}
