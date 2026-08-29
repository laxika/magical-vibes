package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "LCI", collectorNumber = "124")
public class StingingCaveCrawler extends Card {

    public StingingCaveCrawler() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new GraveyardCardThreshold(4, new CardIsPermanentPredicate()),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))));
    }
}
