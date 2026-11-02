package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "9ED", collectorNumber = "224")
@CardRegistration(set = "POR", collectorNumber = "152")
@CardRegistration(set = "WTH", collectorNumber = "116")
public class Thundermare extends Card {

    public Thundermare() {
        // Haste is auto-loaded from Scryfall.
        // When this creature enters, tap all other creatures.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
    }
}
