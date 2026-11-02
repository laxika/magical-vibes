package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardThenDrawEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "142")
public class SkullknockerOgre extends Card {

    public SkullknockerOgre() {
        addTrigger(Triggers.dealsDamageToOpponent(),
                new TargetPlayerRandomDiscardThenDrawEffect());
    }
}
