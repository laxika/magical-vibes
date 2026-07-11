package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "97")
public class HowlingFury extends Card {

    public HowlingFury() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 0));
    }
}
