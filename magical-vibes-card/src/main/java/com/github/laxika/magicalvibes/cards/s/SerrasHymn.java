package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "48")
public class SerrasHymn extends Card {

    public SerrasHymn() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCountersOnSelfEffect(CounterType.VERSE),
                "Put a verse counter on Serra's Hymn?"));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(),
                        new PreventDividedDamageEffect(new CountersOnSource(CounterType.VERSE))),
                "Sacrifice this enchantment: Prevent the next X damage that would be dealt this turn to any number of targets, divided as you choose, where X is the number of verse counters on this enchantment."));
    }
}
