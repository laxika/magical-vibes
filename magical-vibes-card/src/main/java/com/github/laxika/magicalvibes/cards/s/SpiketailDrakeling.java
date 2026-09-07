package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "80")
public class SpiketailDrakeling extends Card {

    public SpiketailDrakeling() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new CounterUnlessPaysEffect(2)),
                "Sacrifice this creature: Counter target spell unless its controller pays {2}."
        ));
    }
}
