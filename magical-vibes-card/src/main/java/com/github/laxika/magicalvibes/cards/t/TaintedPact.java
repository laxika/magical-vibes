package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TaintedPactEffect;

@CardRegistration(set = "ODY", collectorNumber = "164")
public class TaintedPact extends Card {

    public TaintedPact() {
        addEffect(EffectSlot.SPELL, new TaintedPactEffect());
    }
}
