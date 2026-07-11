package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "17")
public class RavagesOfWar extends Card {

    public RavagesOfWar() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsLandPredicate()));
    }
}
