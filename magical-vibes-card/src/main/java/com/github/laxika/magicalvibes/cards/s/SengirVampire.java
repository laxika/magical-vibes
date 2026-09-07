package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "10E", collectorNumber = "176")
@CardRegistration(set = "9ED", collectorNumber = "161")
@CardRegistration(set = "4ED", collectorNumber = "160")
@CardRegistration(set = "M12", collectorNumber = "107")
@CardRegistration(set = "M14", collectorNumber = "113")
@CardRegistration(set = "BRB", collectorNumber = "69")
@CardRegistration(set = "BTD", collectorNumber = "29")
@CardRegistration(set = "SUM", collectorNumber = "129")
@CardRegistration(set = "TOR", collectorNumber = "80")
public class SengirVampire extends Card {

    public SengirVampire() {
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
