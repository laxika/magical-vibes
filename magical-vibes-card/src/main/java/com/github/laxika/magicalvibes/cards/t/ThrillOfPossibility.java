package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "FDN", collectorNumber = "210")
@CardRegistration(set = "M21", collectorNumber = "165")
@CardRegistration(set = "ONE", collectorNumber = "151")
@CardRegistration(set = "ELD", collectorNumber = "146")
@CardRegistration(set = "THB", collectorNumber = "159")
public class ThrillOfPossibility extends Card {

    public ThrillOfPossibility() {
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
