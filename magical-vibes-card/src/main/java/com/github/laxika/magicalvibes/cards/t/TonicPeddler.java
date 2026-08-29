package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "54")
public class TonicPeddler extends Card {

    public TonicPeddler() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new TargetPlayerGainsLifeEffect(3)
                ),
                "{W}, {T}, Discard a card: Target player gains 3 life."
        ));
    }
}
