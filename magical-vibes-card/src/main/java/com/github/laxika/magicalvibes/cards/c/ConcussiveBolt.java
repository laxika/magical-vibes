package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCreaturesCantBlockThisTurnEffect;

@CardRegistration(set = "MBS", collectorNumber = "60")
public class ConcussiveBolt extends Card {

    public ConcussiveBolt() {
        // Concussive Bolt deals 4 damage to target player.
        // Metalcraft — If you control three or more artifacts, creatures that player controls
        // can't block this turn.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerEffect(4));
        addEffect(EffectSlot.SPELL, new MetalcraftConditionalEffect(
                new TargetPlayerCreaturesCantBlockThisTurnEffect()));
    }
}
