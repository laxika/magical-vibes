package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "BNG", collectorNumber = "76")
public class MarshmistTitan extends Card {

    public MarshmistTitan() {
        // This spell costs {X} less to cast, where X is your devotion to black.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.BLACK)));
    }
}
