package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

public class LikenessOfTheSeeker extends Card {

    public LikenessOfTheSeeker() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate(), 3));
    }
}
