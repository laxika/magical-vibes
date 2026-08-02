package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

@CardRegistration(set = "CHK", collectorNumber = "13")
public class HoldTheLine extends Card {

    public HoldTheLine() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(7, 7, new PermanentIsBlockingPredicate()));
    }
}
