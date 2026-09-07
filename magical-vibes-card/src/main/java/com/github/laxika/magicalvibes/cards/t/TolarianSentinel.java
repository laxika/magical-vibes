package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "87")
public class TolarianSentinel extends Card {

    public TolarianSentinel() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new DiscardCardTypeCost(null, null), ReturnToHandEffect.target()),
                "{U}, {T}, Discard a card: Return target permanent you control to its owner's hand.",
                TargetFilters.permanentYouControl()
        ));
    }
}
