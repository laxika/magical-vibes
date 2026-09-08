package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberBoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "M21", collectorNumber = "37")
public class SiegeStriker extends Card {

    public SiegeStriker() {
        addEffect(EffectSlot.ON_ATTACK,
                new TapAnyNumberBoostSelfEffect(new PermanentIsCreaturePredicate(), 1, 1));
    }
}
