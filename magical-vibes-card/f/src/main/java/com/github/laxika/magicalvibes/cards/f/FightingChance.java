package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlipCoinForEachBlockingCreatureEffect;

@CardRegistration(set = "EXO", collectorNumber = "82")
public class FightingChance extends Card {

    public FightingChance() {
        addSpellEffect(new FlipCoinForEachBlockingCreatureEffect());
    }
}
