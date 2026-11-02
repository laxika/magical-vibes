package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "55")
public class LibraryLarcenist extends Card {

    public LibraryLarcenist() {
        addTrigger(Triggers.attacks(), new DrawCardEffect(1));
    }
}
