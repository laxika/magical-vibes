package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentBlockingSourcePredicate;

@CardRegistration(set = "RNA", collectorNumber = "82")
public class PlagueWight extends Card {

    public PlagueWight() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new BoostAllCreaturesEffect(-1, -1, new PermanentBlockingSourcePredicate()));
    }
}
