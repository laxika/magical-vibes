package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;

@CardRegistration(set = "CHK", collectorNumber = "30")
public class KondaLordOfEiganjo extends Card {

    public KondaLordOfEiganjo() {
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(5));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(5));
    }
}
