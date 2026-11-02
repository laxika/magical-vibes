package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "153")
@CardRegistration(set = "OHOP", collectorNumber = "29")
@CardRegistration(set = "OPCA", collectorNumber = "62")
public class Panopticon extends Card {
    public Panopticon() {
        addTrigger(Triggers.planeswalkToThis(), new DrawCardEffect(1));
        addTrigger(Triggers.drawStep(ActivePlayer.YOU), new DrawCardEffect(1));
        addTrigger(Triggers.chaosEnsues(), new DrawCardEffect(1));
    }
}
