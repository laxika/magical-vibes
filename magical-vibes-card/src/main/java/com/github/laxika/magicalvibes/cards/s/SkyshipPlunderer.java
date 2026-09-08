package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfEachKindToTargetEffect;

@CardRegistration(set = "AER", collectorNumber = "46")
public class SkyshipPlunderer extends Card {

    public SkyshipPlunderer() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new AddAnotherCounterOfEachKindToTargetEffect());
    }
}
