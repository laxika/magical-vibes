package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect;

@CardRegistration(set = "JUD", collectorNumber = "81")
public class BreakingPoint extends Card {

    public BreakingPoint() {
        addEffect(EffectSlot.SPELL, new AnyPlayerMayTakeDamageOrDestroyAllCreaturesEffect(6));
    }
}
