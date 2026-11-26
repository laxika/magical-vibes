package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "189")
public class TimbermawLarva extends Card {

    public TimbermawLarva() {
        PermanentCount forestsYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER);
        addTrigger(Triggers.attacks(), BoostEffect.self(forestsYouControl, forestsYouControl));
    }
}
