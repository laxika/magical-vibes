package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;

@CardRegistration(set = "STX", collectorNumber = "170")
public class CramSession extends Card {

    public CramSession() {
        addSpellEffect(new GainLifeEffect(4));
        addSpellEffect(new LearnEffect());
    }
}
