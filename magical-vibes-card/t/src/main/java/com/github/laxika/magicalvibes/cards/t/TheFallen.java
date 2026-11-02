package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToOpponentsAndPlaneswalkersDamagedBySourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHR", collectorNumber = "38")
@CardRegistration(set = "DRK", collectorNumber = "53")
@CardRegistration(set = "ME1", collectorNumber = "69")
public class TheFallen extends Card {

    public TheFallen() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new DealDamageToOpponentsAndPlaneswalkersDamagedBySourceEffect(1));
    }
}
