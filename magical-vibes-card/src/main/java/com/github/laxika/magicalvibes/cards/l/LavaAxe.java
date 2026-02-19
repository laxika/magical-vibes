package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "215")
public class LavaAxe extends Card {

    public LavaAxe() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(5));
    }
}
