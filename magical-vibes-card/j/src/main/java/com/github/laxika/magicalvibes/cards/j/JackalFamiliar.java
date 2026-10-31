package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "143")
public class JackalFamiliar extends Card {

    public JackalFamiliar() {
        addStaticEffect(new CantAttackOrBlockAloneEffect());
    }
}
