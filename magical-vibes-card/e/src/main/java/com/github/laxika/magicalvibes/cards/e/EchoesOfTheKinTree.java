package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "15")
public class EchoesOfTheKinTree extends Card {

    public EchoesOfTheKinTree() {
        // {2}{W}: Bolster 1.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new BolsterEffect(1)),
                "{2}{W}: Bolster 1."
        ));
    }
}
