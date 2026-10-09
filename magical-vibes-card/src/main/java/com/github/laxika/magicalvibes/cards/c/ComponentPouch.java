package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "59")
public class ComponentPouch extends Card {

    public ComponentPouch() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.COMPONENT),
                        AwardAnyColorManaEffect.ofDifferentColors(2)),
                "{T}, Remove a component counter from Component Pouch: Add two mana of different colors."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new RollD20Effect(
                        new PutCountersOnSelfEffect(CounterType.COMPONENT),
                        new PutCountersOnSelfEffect(CounterType.COMPONENT, 2))),
                "{T}: Roll a d20."
        ));
    }
}
