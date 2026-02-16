package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

public class SpiketailHatchling extends Card {

    public SpiketailHatchling() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new CounterUnlessPaysEffect(1)),
                false,
                true,
                "Sacrifice Spiketail Hatchling: Counter target spell unless its controller pays {1}.",
                null
        ));
    }
}
