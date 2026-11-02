package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "36")
public class KnightOfTheMists extends Card {

    public KnightOfTheMists() {
        // Flanking is auto-loaded from Scryfall and handled by the engine.
        // When this creature enters, you may pay {U}. If you don't, destroy target Knight
        // and it can't be regenerated.
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.KNIGHT),
                "Target must be a Knight"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), new MayPayManaEffect(
                "{U}",
                null,
                "Pay {U}?",
                new DestroyTargetPermanentEffect(true)));
    }
}
