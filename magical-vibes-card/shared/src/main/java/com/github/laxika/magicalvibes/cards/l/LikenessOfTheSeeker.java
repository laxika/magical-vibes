package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class LikenessOfTheSeeker extends Card {

    public LikenessOfTheSeeker() {
        addTrigger(Triggers.becomesBlocked(),
                new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(),
                        new PermanentIsLandPredicate()), null, 3));
    }
}
