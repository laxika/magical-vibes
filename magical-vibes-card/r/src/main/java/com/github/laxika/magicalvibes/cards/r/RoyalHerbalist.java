package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "15a")
@CardRegistration(set = "ALL", collectorNumber = "15b")
public class RoyalHerbalist extends Card {

    public RoyalHerbalist() {
        // {2}, Exile the top card of your library: You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new ExileTopCardOfLibraryCost(1), new GainLifeEffect(1)),
                "{2}, Exile the top card of your library: You gain 1 life."
        ));
    }
}
