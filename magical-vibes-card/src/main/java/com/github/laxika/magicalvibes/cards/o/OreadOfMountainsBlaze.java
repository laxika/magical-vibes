package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "146")
public class OreadOfMountainsBlaze extends Card {

    public OreadOfMountainsBlaze() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect()),
                "{2}{R}, Discard a card: Draw a card."
        ));
    }
}
