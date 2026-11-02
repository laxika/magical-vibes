package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealFirstDrawCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "126")
public class PrimitiveEtchings extends Card {

    public PrimitiveEtchings() {
        addTrigger(Triggers.youDrawCard(), new RevealFirstDrawCreatureEffect());
    }
}
