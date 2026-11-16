package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;

@CardRegistration(set = "ULG", collectorNumber = "113")
@CardRegistration(set = "VMA", collectorNumber = "232")
public class SimianGrunts extends Card {

    public SimianGrunts() {
        addStaticEffect(new EchoEffect("{2}{G}"));
    }
}
