package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "CHK", collectorNumber = "128")
public class NezumiCutthroat extends Card {

    public NezumiCutthroat() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
