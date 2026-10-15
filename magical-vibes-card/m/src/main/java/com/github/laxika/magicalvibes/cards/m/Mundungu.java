package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "132")
public class Mundungu extends Card {

    public Mundungu() {
        // {T}: Counter target spell unless its controller pays {1} and 1 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CounterUnlessPaysEffect(1, 1)),
                "{T}: Counter target spell unless its controller pays {1} and 1 life."
        ));
    }
}
