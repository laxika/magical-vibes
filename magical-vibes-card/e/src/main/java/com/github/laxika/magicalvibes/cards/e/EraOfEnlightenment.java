package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HandOfEnlightenment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "11")
public class EraOfEnlightenment extends Card {

    public EraOfEnlightenment() {
        setBackFaceCard(new HandOfEnlightenment());

        addTrigger(Triggers.chapter(1), new ScryEffect(2));
        addTrigger(Triggers.chapter(2), new GainLifeEffect(2));
        addTrigger(Triggers.chapter(3),
                new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "HandOfEnlightenment";
    }
}
