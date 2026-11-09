package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "117")
public class BerserkMurlodont extends Card {

    public BerserkMurlodont() {
        // Whenever a Beast becomes blocked, that Beast gets +1/+1 until end of turn for each
        // creature blocking it.
        addTrigger(Triggers.creatureBecomesBlocked().withFilter(new PermanentHasSubtypePredicate(CardSubtype.BEAST)),
                new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING,
                        new CreaturesBlockingSource(true), new CreaturesBlockingSource(true)));
    }
}
