package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "C16", collectorNumber = "54")
public class CrystallineCrawler extends Card {

    public CrystallineCrawler() {
        // Converge — This creature enters with a +1/+1 counter on it for each color of mana spent
        // to cast it. Converge carries the distinct-color count as the stack entry's X value.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        // Remove a +1/+1 counter from this creature: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new AwardAnyColorManaEffect()
                ),
                "Remove a +1/+1 counter from this creature: Add one mana of any color."
        ));

        // {T}: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{T}: Put a +1/+1 counter on this creature."
        ));
    }
}
