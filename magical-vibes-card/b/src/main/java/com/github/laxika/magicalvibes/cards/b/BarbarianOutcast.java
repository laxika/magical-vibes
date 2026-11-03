package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "92")
public class BarbarianOutcast extends Card {

    public BarbarianOutcast() {
        // "When you control no Swamps, sacrifice this creature." — state-triggered ability.
        addTrigger(Triggers.sourceState(new PermanentNotPredicate(new PermanentControllerControlsPermanentPredicate(
                        new PermanentHasSubtypePredicate(CardSubtype.SWAMP)))),
                new SacrificeSelfEffect());
    }
}
