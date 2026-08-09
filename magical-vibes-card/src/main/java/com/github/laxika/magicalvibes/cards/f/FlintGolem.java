package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillDefendingPlayerEffect;

@CardRegistration(set = "NEM", collectorNumber = "130")
public class FlintGolem extends Card {

    public FlintGolem() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new MillDefendingPlayerEffect(3));
    }
}
