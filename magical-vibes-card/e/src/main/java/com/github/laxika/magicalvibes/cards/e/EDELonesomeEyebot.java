package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesGreaterThanSourceCounters;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "131")
@CardRegistration(set = "PIP", collectorNumber = "432")
@CardRegistration(set = "PIP", collectorNumber = "659")
@CardRegistration(set = "PIP", collectorNumber = "960")
public class EDELonesomeEyebot extends Card {

    public EDELonesomeEyebot() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(
                        new AttackingCreaturesGreaterThanSourceCounters(CounterType.QUEST),
                        new PutCountersOnSelfEffect(CounterType.QUEST)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        new DrawCardEffect(1),
                        new DrawCardEffect(new CountersOnSource(CounterType.QUEST))),
                "{2}, Sacrifice ED-E: Draw a card, then draw an additional card for each quest counter on ED-E."
        ));
    }
}
