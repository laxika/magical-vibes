package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "83")
public class TerisianMindbreaker extends Card {

    public TerisianMindbreaker() {
        addTrigger(Triggers.attacks(), new MillHalfLibraryEffect(true, Subjects.defendingPlayer()));
        addUnearth("{1}{U}{U}{U}");
    }
}
