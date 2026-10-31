package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "AKH", collectorNumber = "76")
@CardRegistration(set = "AKR", collectorNumber = "90")
public class WindsOfRebuke extends Card {

    public WindsOfRebuke() {
        // Return target nonland permanent to its owner's hand.
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "Target must be a nonland permanent"
        )).addSpellEffect(ReturnToHandEffect.target());
        // Each player mills two cards.
        addSpellEffect(new MillEffect(2, MillRecipient.CONTROLLER));
        addSpellEffect(new MillEffect(2, MillRecipient.EACH_OPPONENT));
    }
}
