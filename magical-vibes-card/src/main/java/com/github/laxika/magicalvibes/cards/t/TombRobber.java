package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "87")
public class TombRobber extends Card {

    public TombRobber() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new DiscardCardTypeCost(null, null), new ExploreEffect()),
                "{1}, Discard a card: This creature explores."
        ));
    }
}
