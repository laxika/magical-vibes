package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "144")
public class QuestForPureFlame extends Card {

    public QuestForPureFlame() {
        addEffect(EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT,
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),
                        "Put a quest counter on Quest for Pure Flame?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(4, CounterType.QUEST),
                        new SacrificeSelfCost(),
                        new DoubleControllerDamageThisTurnEffect()
                ),
                "Remove four quest counters from Quest for Pure Flame and sacrifice it: If any source "
                        + "you control would deal damage to a permanent or player this turn, it deals "
                        + "double that damage to that permanent or player instead."
        ));
    }
}
