package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessSacrificeNonlandOrDiscardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "78")
@CardRegistration(set = "HA4", collectorNumber = "11")
public class TormentOfScarabs extends Card {

    public TormentOfScarabs() {
        // At the beginning of enchanted player's upkeep, that player loses 3 life unless they
        // sacrifice a nonland permanent of their choice or discard a card.
        addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER),
                new LoseLifeUnlessSacrificeNonlandOrDiscardEffect(3));
    }
}
