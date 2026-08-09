package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M15", collectorNumber = "41")
@CardRegistration(set = "STH", collectorNumber = "22")
public class WallOfEssence extends Card {

    public WallOfEssence() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_SELF, new GainLifeEffect(new EventValue()));
    }
}
