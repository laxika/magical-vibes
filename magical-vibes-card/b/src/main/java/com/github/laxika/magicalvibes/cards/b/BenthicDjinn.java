package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "MIR", collectorNumber = "257")
public class BenthicDjinn extends Card {

    public BenthicDjinn() {
        // Islandwalk is a Scryfall-loaded keyword.
        // At the beginning of your upkeep, you lose 2 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LoseLifeEffect(2));
    }
}
