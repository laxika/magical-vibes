package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMID", collectorNumber = "44")
@CardRegistration(set = "MB2", collectorNumber = "261")
public class ToralfsDisciple extends Card {

    public ToralfsDisciple() {
        addTrigger(Triggers.attacks(),
                new ConjureCardNamedIntoLibraryEffect("LEA", "161", 4));
    }
}
