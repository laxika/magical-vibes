package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "110")
public class TreasureDredger extends Card {

    public TreasureDredger() {
        // {1}, {T}, Pay 1 life: Create a Treasure token.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new PayLifeCost(1), CreateTokenEffect.ofTreasureToken(1)),
                "{1}, {T}, Pay 1 life: Create a Treasure token."
        ));
    }
}
