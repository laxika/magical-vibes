package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "6")
public class GhostTactician extends Card {

    public GhostTactician() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        BoostEffect.creaturesYouControl(1, 0)
                ),
                "{W}, {T}, Discard a card: Creatures you control get +1/+0 until end of turn."
        ));
    }
}
