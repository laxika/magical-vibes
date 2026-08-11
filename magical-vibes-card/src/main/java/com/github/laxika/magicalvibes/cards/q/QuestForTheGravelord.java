package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZEN", collectorNumber = "108")
public class QuestForTheGravelord extends Card {

    public QuestForTheGravelord() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),
                        "Put a quest counter on Quest for the Gravelord?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.QUEST),
                        new SacrificeSelfCost(),
                        new CreateTokenEffect("Zombie Giant", 5, 5, CardColor.BLACK,
                                List.of(CardSubtype.ZOMBIE, CardSubtype.GIANT), Set.of(), Set.of())
                ),
                "Remove three quest counters from Quest for the Gravelord and sacrifice it: Create a 5/5 black Zombie Giant creature token."
        ));
    }
}
