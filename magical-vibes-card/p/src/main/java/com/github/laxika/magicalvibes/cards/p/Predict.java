package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NameCardMillTargetDrawEffect;

@CardRegistration(set = "ODY", collectorNumber = "94")
@CardRegistration(set = "SLD", collectorNumber = "2156")
@CardRegistration(set = "C18", collectorNumber = "98")
public class Predict extends Card {

    public Predict() {
        addSpellEffect(new NameCardMillTargetDrawEffect());
        addSpellEffect(new DrawCardEffect(1));
    }
}
