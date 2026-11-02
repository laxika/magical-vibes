package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "167")
public class JadeAvenger extends Card {

    public JadeAvenger() {
        addTrigger(Triggers.blocks(), new BushidoEffect(2));
        addTrigger(Triggers.becomesBlocked(), new BushidoEffect(2));
    }
}
