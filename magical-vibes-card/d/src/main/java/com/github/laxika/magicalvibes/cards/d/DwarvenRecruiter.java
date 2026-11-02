package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSubtypeCardsToTopEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "186")
public class DwarvenRecruiter extends Card {

    public DwarvenRecruiter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SearchLibraryForSubtypeCardsToTopEffect(CardSubtype.DWARF));
    }
}
