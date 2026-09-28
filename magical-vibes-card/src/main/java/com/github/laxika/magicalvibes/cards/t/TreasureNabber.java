package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;

@CardRegistration(set = "CMM", collectorNumber = "265")
@CardRegistration(set = "CMM", collectorNumber = "552")
@CardRegistration(set = "CMM", collectorNumber = "645")
@CardRegistration(set = "LTC", collectorNumber = "230")
public class TreasureNabber extends Card {

    public TreasureNabber() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA,
                new GainControlOfTargetEffect(ControlDuration.UNTIL_END_OF_YOUR_NEXT_TURN));
    }
}
