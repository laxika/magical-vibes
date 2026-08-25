package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "139")
public class UrborgSyphonMage extends Card {

    public UrborgSyphonMage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT, true)
                ),
                "{2}{B}, {T}, Discard a card: Each other player loses 2 life. You gain life equal to the life lost this way."
        ));
    }
}
