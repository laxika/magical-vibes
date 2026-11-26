package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "GS1", collectorNumber = "14")
public class QilinsBlessing extends Card {

    public QilinsBlessing() {
        // Target creature gets +2/+2 until end of turn.
        addSpellEffect(BoostEffect.target(2, 2));
    }
}
