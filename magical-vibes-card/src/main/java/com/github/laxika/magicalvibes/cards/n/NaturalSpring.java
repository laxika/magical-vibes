package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

@CardRegistration(set = "10E", collectorNumber = "281")
public class NaturalSpring extends Card {

    public NaturalSpring() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new TargetPlayerGainsLifeEffect(8));
    }
}
