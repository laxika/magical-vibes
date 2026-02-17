package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

public class FountainOfYouth extends Card {

    public FountainOfYouth() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new GainLifeEffect(1)),
                false,
                "{2}, {T}: You gain 1 life."
        ));
    }
}
