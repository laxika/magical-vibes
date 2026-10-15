package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "249")
public class AvishkarRaceway extends Card {

    public AvishkarRaceway() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect(1)),
                "Max speed — {3}, {T}, Discard a card: Draw a card."
        ).withActivationCondition(new MaxSpeed(), "Activate only if you have max speed"));
    }
}
