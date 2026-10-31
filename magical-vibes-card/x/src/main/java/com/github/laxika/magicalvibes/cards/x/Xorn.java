package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AddTokenCreationEffect;

@CardRegistration(set = "AFR", collectorNumber = "167")
@CardRegistration(set = "SLD", collectorNumber = "876")
@CardRegistration(set = "SLD", collectorNumber = "2495")
public class Xorn extends Card {

    public Xorn() {
        addStaticEffect(new AddTokenCreationEffect(1, CardSubtype.TREASURE));
    }
}
