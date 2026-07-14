package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect;

@CardRegistration(set = "EVE", collectorNumber = "120")
public class Doomgape extends Card {

    public Doomgape() {
        // At the beginning of your upkeep, sacrifice a creature. You gain life equal to that
        // creature's toughness. (Trample is auto-loaded from Scryfall.)
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect(true));
    }
}
