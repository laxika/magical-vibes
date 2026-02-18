package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;

public class LavaAxe extends Card {

    public LavaAxe() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(5));
    }
}
