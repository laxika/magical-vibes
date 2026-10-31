package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "ISD", collectorNumber = "63")
public class LostInTheMist extends Card {

    public LostInTheMist() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(ReturnToHandEffect.target());
    }
}
