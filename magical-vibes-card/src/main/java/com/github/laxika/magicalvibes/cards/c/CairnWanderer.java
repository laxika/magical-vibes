package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsInAllGraveyardsEffect;

@CardRegistration(set = "LRW", collectorNumber = "105")
public class CairnWanderer extends Card {

    public CairnWanderer() {
        addEffect(EffectSlot.STATIC, new GainKeywordsOfCreatureCardsInAllGraveyardsEffect());
    }
}
