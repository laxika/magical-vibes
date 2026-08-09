package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "MRD", collectorNumber = "50")
public class SlithStrider extends Card {

    public SlithStrider() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DrawCardEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
