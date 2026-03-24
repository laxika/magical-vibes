package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;

@CardRegistration(set = "XLN", collectorNumber = "163")
public class StormFleetPyromancer extends Card {

    public StormFleetPyromancer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RaidConditionalEffect(new DealDamageToAnyTargetEffect(2)));
    }
}
