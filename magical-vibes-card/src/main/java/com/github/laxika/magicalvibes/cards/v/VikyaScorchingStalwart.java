package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "SLX", collectorNumber = "11")
public class VikyaScorchingStalwart extends Card {

    public VikyaScorchingStalwart() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new DealDamageToAnyTargetEffect(new SourcePower()),
                        new ConditionalEffect(new EventValueAtLeast(1), new DrawCardEffect())
                ),
                "{4}{R}, {Q}, Discard a card: Vikya, Scorching Stalwart deals damage equal to its power to any target. "
                        + "If excess damage was dealt to a creature this way, draw a card."
        ).withRequiresUntap());
    }
}
