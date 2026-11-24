package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

public class WretchedBonemass extends Card {

    public WretchedBonemass() {
        TotalPowerOfCardsExiledWithSource totalPower = new TotalPowerOfCardsExiledWithSource();
        addStaticEffect(new SetPowerToughnessToAmountEffect(totalPower, totalPower));
        addStaticEffect(new GainKeywordsOfCreatureCardsExiledWithSourceEffect(Subjects.exiledWithSource(),
                GainKeywordsOfCreatureCardsExiledWithSourceEffect.COMMON_KEYWORDS, true));
    }
}
