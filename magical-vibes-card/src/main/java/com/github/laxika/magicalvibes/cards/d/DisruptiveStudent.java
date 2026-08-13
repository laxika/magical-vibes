package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "69")
public class DisruptiveStudent extends Card {

    public DisruptiveStudent() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CounterUnlessPaysEffect(1)),
                "{T}: Counter target spell unless its controller pays {1}."
        ));
    }
}
