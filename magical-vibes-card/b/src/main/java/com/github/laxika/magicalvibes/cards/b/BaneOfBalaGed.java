package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerChoosesPermanentsToExileEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIO", collectorNumber = "1")
public class BaneOfBalaGed extends Card {

    public BaneOfBalaGed() {
        addTrigger(Triggers.attacks(), new DefendingPlayerChoosesPermanentsToExileEffect(2));
    }
}
