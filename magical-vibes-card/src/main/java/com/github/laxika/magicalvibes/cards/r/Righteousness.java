package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetBlockingCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "36")
public class Righteousness extends Card {

    public Righteousness() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new BoostTargetBlockingCreatureEffect(7, 7));
    }
}
