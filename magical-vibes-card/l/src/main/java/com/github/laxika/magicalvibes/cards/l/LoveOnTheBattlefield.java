package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.ExactlyAttackers;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "87")
@CardRegistration(set = "MSC", collectorNumber = "408")
public class LoveOnTheBattlefield extends Card {

    public LoveOnTheBattlefield() {
        // The two attackers are fixed when the trigger fires: Subjects.triggeringAttackers() reads the entry's
        // attacking creatures, so a creature that left combat before resolution still gains first strike.
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withEventCondition(new ExactlyAttackers(2)),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.triggeringAttackers()),
                new DrawCardEffect(1),
                new GrantEffectToTargetUntilEndOfCombatEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                        Subjects.narrowed(Subjects.creaturesYouControl(), new PermanentIsAttackingPredicate())));
    }
}
