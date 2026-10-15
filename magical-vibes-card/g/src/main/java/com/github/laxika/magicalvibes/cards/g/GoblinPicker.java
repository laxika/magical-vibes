package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "128")
public class GoblinPicker extends Card {

    public GoblinPicker() {
        // {R}, {T}, Discard a card: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect()),
                "{R}, {T}, Discard a card: Draw a card."
        ));
    }
}
