package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;


@CardRegistration(set = "AKH", collectorNumber = "66")
public class RiverSerpent extends Card {

    public RiverSerpent() {
        // This creature can't attack unless there are five or more cards in your graveyard.
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new GraveyardCardThreshold(5, null),
                "five or more cards in your graveyard"
        ));

        // Cycling {U} ({U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{U}");
    }
}
