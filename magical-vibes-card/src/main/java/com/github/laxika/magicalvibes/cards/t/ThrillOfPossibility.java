package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "FDN", collectorNumber = "210")
@CardRegistration(set = "STA", collectorNumber = "46")
@CardRegistration(set = "M21", collectorNumber = "165")
@CardRegistration(set = "ONE", collectorNumber = "151")
@CardRegistration(set = "ELD", collectorNumber = "146")
@CardRegistration(set = "THB", collectorNumber = "159")
@CardRegistration(set = "SLD", collectorNumber = "1805")
@CardRegistration(set = "DMU", collectorNumber = "148")
@CardRegistration(set = "MAR", collectorNumber = "28")
@CardRegistration(set = "OMB", collectorNumber = "28")
@CardRegistration(set = "LTC", collectorNumber = "229")
public class ThrillOfPossibility extends Card {

    public ThrillOfPossibility() {
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
