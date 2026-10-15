package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesInsteadOfDyingThisTurnEffect;

@CardRegistration(set = "OGW", collectorNumber = "70")
public class FlayingTendrils extends Card {

    public FlayingTendrils() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, -2));
        addEffect(EffectSlot.SPELL, new ExileCreaturesInsteadOfDyingThisTurnEffect());
    }
}
