package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

@CardRegistration(set = "4ED", collectorNumber = "41")
public class Piety extends Card {

    public Piety() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(0, 3, new PermanentIsBlockingPredicate()));
    }
}
