package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "49")
public class IorRuinExpedition extends Card {

    public IorRuinExpedition() {
        // Landfall — Whenever a land you control enters, you may put a quest counter on this enchantment.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MayEffect(new PutCountersOnSelfEffect(CounterType.QUEST),
                        "Put a quest counter on Ior Ruin Expedition?"));

        // Remove three quest counters from this enchantment and sacrifice it: Draw two cards.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.QUEST),
                        new SacrificeSelfCost(),
                        new DrawCardEffect(2)
                ),
                "Remove three quest counters from Ior Ruin Expedition and sacrifice it: Draw two cards."
        ));
    }
}
