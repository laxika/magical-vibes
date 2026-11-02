package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DST", collectorNumber = "83")
public class RoaringSlagwurm extends Card {

    public RoaringSlagwurm() {
        addTrigger(Triggers.attacks(),
                new TapPermanentsEffect(TapUntapScope.ALL_PERMANENTS, new PermanentIsArtifactPredicate()));
    }
}
