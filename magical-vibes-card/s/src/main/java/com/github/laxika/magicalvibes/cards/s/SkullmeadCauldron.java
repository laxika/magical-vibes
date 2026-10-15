package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "167")
public class SkullmeadCauldron extends Card {

    public SkullmeadCauldron() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GainLifeEffect(1)),
                "{T}: You gain 1 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DiscardCardTypeCost(null, null), new GainLifeEffect(3)),
                "{T}, Discard a card: You gain 3 life."
        ));
    }
}
