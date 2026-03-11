package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "M11", collectorNumber = "129")
public class ChandrasSpitfire extends Card {

    public ChandrasSpitfire() {
        addEffect(EffectSlot.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, new BoostSelfEffect(3, 0));
    }
}
