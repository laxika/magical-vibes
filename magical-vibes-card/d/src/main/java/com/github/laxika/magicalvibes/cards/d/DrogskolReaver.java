package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "137")
@CardRegistration(set = "2X2", collectorNumber = "205")
public class DrogskolReaver extends Card {

    public DrogskolReaver() {
        addTrigger(Triggers.youGainLife(), new DrawCardEffect(1));
    }
}
