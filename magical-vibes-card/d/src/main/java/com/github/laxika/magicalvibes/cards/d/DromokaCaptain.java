package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "12")
public class DromokaCaptain extends Card {

    public DromokaCaptain() {
        // Whenever this creature attacks, bolster 1.
        addTrigger(Triggers.attacks(), new BolsterEffect(1));
    }
}
