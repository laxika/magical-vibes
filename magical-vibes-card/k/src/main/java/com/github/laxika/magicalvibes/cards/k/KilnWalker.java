package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "NPH", collectorNumber = "142")
public class KilnWalker extends Card {

    public KilnWalker() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(3, 0));
    }
}
