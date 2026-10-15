package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceTargetedCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReselectTargetAtRandomEffect;

@CardRegistration(set = "MB1", collectorNumber = "54")
public class HighTroller extends Card {

    public HighTroller() {
        addEffect(EffectSlot.STATIC, new ReduceTargetedCostEffect(2));
        addEffect(EffectSlot.ON_ANY_PLAYER_CHOOSES_TARGETS, new ReselectTargetAtRandomEffect());
    }
}
