package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "STH", collectorNumber = "130")
@CardRegistration(set = "TPR", collectorNumber = "213")
@CardRegistration(set = "TSB", collectorNumber = "101")
@CardRegistration(set = "H09", collectorNumber = "14")
public class SpinedSliver extends Card {

    public SpinedSliver() {
        // Whenever a Sliver becomes blocked, that Sliver gets +1/+1 until end of turn
        // for each creature blocking it.
        addTrigger(Triggers.creatureBecomesBlocked().withFilter(new PermanentHasSubtypePredicate(CardSubtype.SLIVER)),
                new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING,
                        new CreaturesBlockingSource(true), new CreaturesBlockingSource(true)));
    }
}
