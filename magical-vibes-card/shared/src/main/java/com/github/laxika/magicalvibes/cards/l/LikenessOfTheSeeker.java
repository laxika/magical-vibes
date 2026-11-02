package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class LikenessOfTheSeeker extends Card {

    public LikenessOfTheSeeker() {
        addTrigger(Triggers.becomesBlocked(),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate(), 3));
    }
}
