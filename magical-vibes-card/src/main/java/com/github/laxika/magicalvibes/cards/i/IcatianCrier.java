package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "23")
public class IcatianCrier extends Card {

    public IcatianCrier() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect(2, "Citizen", 1, 1,
                                CardColor.WHITE, List.of(CardSubtype.CITIZEN), Set.of(), Set.of())
                ),
                "{1}{W}, {T}, Discard a card: Create two 1/1 white Citizen creature tokens."
        ));
    }
}
