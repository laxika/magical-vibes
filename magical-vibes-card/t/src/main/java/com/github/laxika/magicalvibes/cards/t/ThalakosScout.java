package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "48")
@CardRegistration(set = "TPR", collectorNumber = "71")
public class ThalakosScout extends Card {

    public ThalakosScout() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, null), ReturnToHandEffect.self()),
                "Discard a card: Return this creature to its owner's hand."
        ));
    }
}
