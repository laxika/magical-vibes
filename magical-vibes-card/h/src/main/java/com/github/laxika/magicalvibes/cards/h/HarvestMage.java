package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.LandManaProducesOneChosenColorUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "105")
public class HarvestMage extends Card {

    public HarvestMage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new LandManaProducesOneChosenColorUntilEndOfTurnEffect()
                ),
                "{G}, {T}, Discard a card: Until end of turn, if you tap a land for mana, it produces one mana of a color of your choice instead of any other type and amount."
        ));
    }
}
