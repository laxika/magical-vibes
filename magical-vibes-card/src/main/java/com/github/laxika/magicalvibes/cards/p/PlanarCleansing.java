package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "24")
public class PlanarCleansing extends Card {

    public PlanarCleansing() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
