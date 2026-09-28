package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "88")
public class HellMongrel extends Card {

    public HellMongrel() {
        // Discard a card: This creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, null), new BoostSelfEffect(1, 1)),
                "Discard a card: This creature gets +1/+1 until end of turn."));
    }
}
