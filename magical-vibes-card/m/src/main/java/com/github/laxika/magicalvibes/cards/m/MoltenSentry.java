package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.FlipCoinOnEnterEffect;

import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "136")
public class MoltenSentry extends Card {

    public MoltenSentry() {
        addStaticEffect(new FlipCoinOnEnterEffect(
                5, 2, Set.of(Keyword.HASTE),
                2, 5, Set.of(Keyword.DEFENDER)));
    }
}
