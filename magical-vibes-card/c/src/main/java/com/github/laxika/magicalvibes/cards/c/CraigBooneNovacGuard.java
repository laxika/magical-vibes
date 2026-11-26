package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DamageFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "100")
@CardRegistration(set = "PIP", collectorNumber = "628")
public class CraigBooneNovacGuard extends Card {

    public CraigBooneNovacGuard() {
        addStaticEffect(new GrantKeywordEffect(Set.of(Keyword.LIFELINK, Keyword.REACH), Subjects.source()));
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(2)),
                SequenceEffect.of(
                        PutCountersEffect.onSelf(CounterType.QUEST, 2),
                        new ConditionalEffect(new HappenedThisWay(StepKind.PUT_COUNTERS), QueueReflexiveAbilityEffect.of(
                                UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.target()),
                                        new DamageFromSourceCost(new CountersOnSource(CounterType.QUEST)),
                                        DealDamageEffect.toTargetCreature(new CountersOnSource(CounterType.QUEST))),
                                true))));
    }
}
