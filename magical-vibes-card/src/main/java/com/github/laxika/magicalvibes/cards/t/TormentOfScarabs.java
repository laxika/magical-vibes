package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessSacrificeNonlandOrDiscardEffect;

@CardRegistration(set = "HOU", collectorNumber = "78")
public class TormentOfScarabs extends Card {

    public TormentOfScarabs() {
        // At the beginning of enchanted player's upkeep, that player loses 3 life unless they
        // sacrifice a nonland permanent of their choice or discard a card.
        addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
                new LoseLifeUnlessSacrificeNonlandOrDiscardEffect(3));
    }
}
