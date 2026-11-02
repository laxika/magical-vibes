package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

// Trample and Persist are loaded from Scryfall; Persist is handled by
// PermanentRemovalService.collectPersistTrigger + PersistReturnEffect.
@CardRegistration(set = "SHM", collectorNumber = "135")
@CardRegistration(set = "MMA", collectorNumber = "174")
@CardRegistration(set = "UMA", collectorNumber = "195")
@CardRegistration(set = "NCC", collectorNumber = "322")
@CardRegistration(set = "LTC", collectorNumber = "264")
public class WoodfallPrimus extends Card {

    public WoodfallPrimus() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                "Target must be a noncreature permanent"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), new DestroyTargetPermanentEffect());
    }
}
