package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "104")
public class SkirsdagSupplicant extends Card {

    public SkirsdagSupplicant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_PLAYER)
                ),
                "{B}, {T}, Discard a card: Each player loses 2 life."
        ));
    }
}
