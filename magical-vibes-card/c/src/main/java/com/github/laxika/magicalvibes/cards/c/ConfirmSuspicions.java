package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "SOI", collectorNumber = "53")
@CardRegistration(set = "SIR", collectorNumber = "56")
@CardRegistration(set = "MKC", collectorNumber = "97")
public class ConfirmSuspicions extends Card {

    public ConfirmSuspicions() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(CreateTokenEffect.ofClueToken(3));
    }
}
