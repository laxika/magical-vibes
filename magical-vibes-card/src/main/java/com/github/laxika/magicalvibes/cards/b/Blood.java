package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2180")
public class Blood extends Card {

    public Blood() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new SacrificeSelfCost(),
                        new DrawCardEffect()
                ),
                "{1}, {T}, Discard a card, Sacrifice this token: Draw a card."
        ));
    }
}
