package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "THB", collectorNumber = "10")
public class DaybreakChimera extends Card {

    public DaybreakChimera() {
        // This spell costs {X} less to cast, where X is your devotion to white.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.WHITE)));
    }
}
