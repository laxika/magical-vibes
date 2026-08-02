package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "TMP", collectorNumber = "47")
public class SoltariTrooper extends Card {

    public SoltariTrooper() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(1, 1));
    }
}
