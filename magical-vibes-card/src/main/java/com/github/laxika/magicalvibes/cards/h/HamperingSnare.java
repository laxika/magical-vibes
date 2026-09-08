package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "IKO", collectorNumber = "55")
public class HamperingSnare extends Card {

    public HamperingSnare() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, 0,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addCycling("{2}");
    }
}
