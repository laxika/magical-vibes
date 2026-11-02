package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "82")
public class PlagueWight extends Card {

    public PlagueWight() {
        addTrigger(Triggers.becomesBlocked(),
                new BoostAllCreaturesEffect(-1, -1, new PermanentBlockingSourcePredicate()));
    }
}
