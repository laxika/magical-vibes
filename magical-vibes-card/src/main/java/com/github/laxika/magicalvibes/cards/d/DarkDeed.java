package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "MSH", collectorNumber = "93")
public class DarkDeed extends Card {

    public DarkDeed() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-4, -4));
    }
}
