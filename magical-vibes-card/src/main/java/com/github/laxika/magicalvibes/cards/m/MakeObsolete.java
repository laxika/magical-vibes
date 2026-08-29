package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "KLD", collectorNumber = "89")
public class MakeObsolete extends Card {

    public MakeObsolete() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-1, -1,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
