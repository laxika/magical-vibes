package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;

@CardRegistration(set = "MH2", collectorNumber = "167")
public class JadeAvenger extends Card {

    public JadeAvenger() {
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(2));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(2));
    }
}
