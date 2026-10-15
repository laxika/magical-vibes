package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "ISD", collectorNumber = "59")
public class HystericalBlindness extends Card {

    public HystericalBlindness() {
        // Creatures your opponents control get -4/-0 until end of turn.
        addEffect(EffectSlot.SPELL,
                new BoostAllCreaturesEffect(-4, 0,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
