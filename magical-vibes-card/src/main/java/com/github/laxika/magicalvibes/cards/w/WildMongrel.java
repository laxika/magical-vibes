package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "283")
public class WildMongrel extends Card {

    public WildMongrel() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new BoostSelfEffect(1, 1),
                        new SetChosenColorUntilEndOfTurnEffect(false, false)
                ),
                "Discard a card: This creature gets +1/+1 and becomes the color of your choice until end of turn."
        ));
    }
}
