package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "12")
@CardRegistration(set = "MMQ", collectorNumber = "11")
public class ChoMannoRevolutionary extends Card {

    public ChoMannoRevolutionary() {
        addStaticEffect(new PreventAllDamageEffect());
    }
}
