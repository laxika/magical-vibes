package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

public class WretchedBonemass extends Card {

    public WretchedBonemass() {
        TotalPowerOfCardsExiledWithSource totalPower = new TotalPowerOfCardsExiledWithSource();
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(totalPower, totalPower));
        addEffect(EffectSlot.STATIC, new GainKeywordsOfCardsExiledWithSourceEffect());
    }
}
