package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.VectorGlider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "66")
public class TheModernAge extends Card {

    public TheModernAge() {
        setBackFaceCard(new VectorGlider());

        addTrigger(Triggers.chapter(1), new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addTrigger(Triggers.chapter(2), new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "VectorGlider";
    }
}
