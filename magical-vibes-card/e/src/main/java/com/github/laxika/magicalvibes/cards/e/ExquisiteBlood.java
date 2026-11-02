package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "102")
@CardRegistration(set = "SLD", collectorNumber = "206")
@CardRegistration(set = "SLD", collectorNumber = "1798")
@CardRegistration(set = "LCC", collectorNumber = "195")
public class ExquisiteBlood extends Card {

    public ExquisiteBlood() {
        // "Whenever an opponent loses life, you gain that much life." The life lost is snapshotted
        // onto the trigger's event value by the collector, read back here as the gain amount.
        addTrigger(Triggers.opponentLosesLife(), new GainLifeEffect(new EventValue()));
    }
}
