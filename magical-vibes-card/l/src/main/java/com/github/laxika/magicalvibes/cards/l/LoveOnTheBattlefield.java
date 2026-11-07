package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.ExactlyAttackers;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "87")
@CardRegistration(set = "MSC", collectorNumber = "408")
public class LoveOnTheBattlefield extends Card {

    public LoveOnTheBattlefield() {
        // The two attackers are fixed when the trigger fires: GrantScope.TARGETS carries the declared
        // attackers, so a creature that left combat before resolution still gains first strike.
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withEventCondition(new ExactlyAttackers(2)),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGETS),
                new DrawCardEffect(1),
                new GrantEffectToOwnCreaturesUntilEndOfCombatEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                        new PutCountersOnSourceEffect(1, 1, 1)),
                        new PermanentIsAttackingPredicate()));
    }
}
