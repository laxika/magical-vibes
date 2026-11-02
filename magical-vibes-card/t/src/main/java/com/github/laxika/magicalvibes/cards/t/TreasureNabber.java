package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "265")
@CardRegistration(set = "CMM", collectorNumber = "552")
@CardRegistration(set = "CMM", collectorNumber = "645")
@CardRegistration(set = "LTC", collectorNumber = "230")
@CardRegistration(set = "C18", collectorNumber = "27")
public class TreasureNabber extends Card {

    public TreasureNabber() {
        addTrigger(Triggers.playerTapsPermanentForMana(),
                new GainControlOfTargetEffect(ControlDuration.UNTIL_END_OF_YOUR_NEXT_TURN));
    }
}
