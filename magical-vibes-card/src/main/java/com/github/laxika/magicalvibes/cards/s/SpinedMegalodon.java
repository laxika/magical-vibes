package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "M21", collectorNumber = "72")
public class SpinedMegalodon extends Card {

    public SpinedMegalodon() {
        addEffect(EffectSlot.ON_ATTACK, new ScryEffect(1));
    }
}
