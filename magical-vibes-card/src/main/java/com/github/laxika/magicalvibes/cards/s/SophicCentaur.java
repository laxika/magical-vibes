package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "139")
public class SophicCentaur extends Card {

    public SophicCentaur() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}{G}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new GainLifeEffect(new Scaled(new CardsInHand(CountScope.CONTROLLER), 2))
                ),
                "{2}{G}{G}, {T}, Discard a card: You gain 2 life for each card in your hand."
        ));
    }
}
