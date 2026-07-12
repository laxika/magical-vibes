package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

// Trample and Persist are loaded from Scryfall; Persist is handled by
// PermanentRemovalService.collectPersistTrigger + PersistReturnEffect.
@CardRegistration(set = "SHM", collectorNumber = "135")
public class WoodfallPrimus extends Card {

    public WoodfallPrimus() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                "Target must be a noncreature permanent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());
    }
}
