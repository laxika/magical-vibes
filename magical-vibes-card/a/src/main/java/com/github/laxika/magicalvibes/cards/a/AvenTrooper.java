package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "2")
public class AvenTrooper extends Card {

    public AvenTrooper() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        BoostEffect.self(1, 2)
                ),
                "{2}{W}, Discard a card: This creature gets +1/+2 until end of turn."
        ));
    }
}
