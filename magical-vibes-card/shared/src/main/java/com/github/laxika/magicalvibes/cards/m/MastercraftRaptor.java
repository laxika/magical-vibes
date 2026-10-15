package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

public class MastercraftRaptor extends Card {

    public MastercraftRaptor() {
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new TotalPowerOfCardsExiledWithSource(), new Fixed(4)));
    }
}
