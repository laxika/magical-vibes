package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "120")
@CardRegistration(set = "DDJ", collectorNumber = "65")
public class Doomgape extends Card {

    public Doomgape() {
        // At the beginning of your upkeep, sacrifice a creature. You gain life equal to that
        // creature's toughness. (Trample is auto-loaded from Scryfall.)
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect(true, true));
    }
}
