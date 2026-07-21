package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "85")
public class BurningFistMinotaur extends Card {

    public BurningFistMinotaur() {
        // First strike auto-loaded from Scryfall.
        // {1}{R}, Discard a card: This creature gets +2/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new DiscardCardTypeCost(null, null), new BoostSelfEffect(2, 0)),
                "{1}{R}, Discard a card: This creature gets +2/+0 until end of turn."));
    }
}
