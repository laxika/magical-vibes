package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "LGN", collectorNumber = "24")
public class WallOfHope extends Card {

    public WallOfHope() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new GainLifeEffect(new EventValue()));
    }
}
