package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "18")
@CardRegistration(set = "A25", collectorNumber = "26")
@CardRegistration(set = "HA2", collectorNumber = "1")
@CardRegistration(set = "TDC", collectorNumber = "126")
public class NyxFleeceRam extends Card {

    public NyxFleeceRam() {
        // At the beginning of your upkeep, you gain 1 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new GainLifeEffect(1));
    }
}
