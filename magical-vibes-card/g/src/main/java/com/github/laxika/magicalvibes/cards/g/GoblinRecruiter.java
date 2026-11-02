package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSubtypeCardsToTopEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "6ED", collectorNumber = "186")
@CardRegistration(set = "VIS", collectorNumber = "80")
@CardRegistration(set = "ATH", collectorNumber = "38")
@CardRegistration(set = "SLD", collectorNumber = "925")
@CardRegistration(set = "SLD", collectorNumber = "926")
@CardRegistration(set = "SLD", collectorNumber = "1313")
@CardRegistration(set = "MB2", collectorNumber = "193")
public class GoblinRecruiter extends Card {

    public GoblinRecruiter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SearchLibraryForSubtypeCardsToTopEffect(CardSubtype.GOBLIN));
    }
}
