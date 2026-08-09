package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

public class IchigaWhoTopplesOaks extends Card {

    public IchigaWhoTopplesOaks() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.KI),
                        new BoostTargetCreatureEffect(2, 2)),
                "Remove a ki counter from Ichiga: Target creature gets +2/+2 until end of turn."));
    }
}
