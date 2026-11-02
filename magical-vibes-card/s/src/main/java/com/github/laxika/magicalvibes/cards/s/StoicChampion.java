package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "21")
@CardRegistration(set = "VMA", collectorNumber = "50")
public class StoicChampion extends Card {

    public StoicChampion() {
        addTrigger(Triggers.playerCyclesCard(), new BoostSelfEffect(2, 2));
    }
}
