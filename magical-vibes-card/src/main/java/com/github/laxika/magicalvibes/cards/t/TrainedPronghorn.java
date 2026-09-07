package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "30")
public class TrainedPronghorn extends Card {

    public TrainedPronghorn() {
        // Discard a card: Prevent all damage that would be dealt to this creature this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, null), PreventDamageEffect.allToSelf()),
                "Discard a card: Prevent all damage that would be dealt to this creature this turn."
        ));
    }
}
