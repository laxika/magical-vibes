package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSameNameAsDyingCreatureToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "34")
public class Remembrance extends Card {

    public Remembrance() {
        addTrigger(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL),
                new MayEffect(new SearchLibraryForSameNameAsDyingCreatureToHandEffect(),
                        "Search for a card with the same name?"));
    }
}
