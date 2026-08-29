package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreepingDreadEffect;

@CardRegistration(set = "SOI", collectorNumber = "104")
public class CreepingDread extends Card {

    public CreepingDread() {
        // At the beginning of your upkeep, each player discards a card. Each opponent who discarded
        // a card sharing a type with the card you discarded loses 3 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CreepingDreadEffect());
    }
}
