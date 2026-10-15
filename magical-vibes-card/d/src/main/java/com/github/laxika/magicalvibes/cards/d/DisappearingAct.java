package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;

@CardRegistration(set = "KLD", collectorNumber = "43")
public class DisappearingAct extends Card {

    public DisappearingAct() {
        addEffect(EffectSlot.SPELL, new ReturnPermanentToHandCost());
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
