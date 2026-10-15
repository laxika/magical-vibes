package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "246")
public class KrosanArcher extends Card {

    public KrosanArcher() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new DiscardCardTypeCost(null, null), new BoostSelfEffect(0, 2)),
                "{G}, Discard a card: Krosan Archer gets +0/+2 until end of turn."
        ));
    }
}
