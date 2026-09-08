package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "WWK", collectorNumber = "73")
public class BullRush extends Card {

    public BullRush() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 0));
    }
}
