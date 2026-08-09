package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "126")
public class BraidwoodCup extends Card {

    public BraidwoodCup() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GainLifeEffect(1)),
                "{T}: You gain 1 life."
        ));
    }
}
