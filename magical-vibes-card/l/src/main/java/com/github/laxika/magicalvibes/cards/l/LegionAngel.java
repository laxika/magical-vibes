package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "23")
public class LegionAngel extends Card {

    public LegionAngel() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SearchOutsideGameForCardToHandEffect(
                new CardNamedPredicate("Legion Angel")));
    }
}
