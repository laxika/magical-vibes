package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "94")
@CardRegistration(set = "ODY", collectorNumber = "91")
@CardRegistration(set = "SLD", collectorNumber = "401")
@CardRegistration(set = "AA3", collectorNumber = "7")
public class Peek extends Card {

    public Peek() {
        addSpellEffect(new LookAtHandEffect());
        addSpellEffect(new DrawCardEffect());
    }
}
