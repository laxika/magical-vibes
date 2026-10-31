package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

public class WretchedBonemass extends Card {

    public WretchedBonemass() {
        TotalPowerOfCardsExiledWithSource totalPower = new TotalPowerOfCardsExiledWithSource();
        addStaticEffect(new SetPowerToughnessToAmountEffect(totalPower, totalPower));
        addStaticEffect(new GainKeywordsOfCardsExiledWithSourceEffect(true));
    }
}
