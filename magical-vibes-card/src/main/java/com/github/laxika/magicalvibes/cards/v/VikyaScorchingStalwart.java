package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetThenDrawIfExcessDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "429")
public class VikyaScorchingStalwart extends Card {

    public VikyaScorchingStalwart() {
        // {4}{R}, {Q}, Discard a card: Vikya deals damage equal to its power to any target. If
        // excess damage was dealt to a creature this way, draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new DealDamageToAnyTargetThenDrawIfExcessDamageEffect(new SourcePower())),
                "{4}{R}, {Q}, Discard a card: Vikya, Scorching Stalwart deals damage equal to its power "
                        + "to any target. If excess damage was dealt to a creature this way, draw a card."
        ).withRequiresUntap());
    }
}
