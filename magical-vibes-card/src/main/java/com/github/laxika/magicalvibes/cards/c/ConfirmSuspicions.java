package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "SOI", collectorNumber = "53")
@CardRegistration(set = "SIR", collectorNumber = "56")
public class ConfirmSuspicions extends Card {

    public ConfirmSuspicions() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofClueToken(3));
    }
}
