package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AvatarKuruk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "61")
public class TheLegendOfKuruk extends Card {

    public TheLegendOfKuruk() {
        setBackFaceCard(new AvatarKuruk());

        addTrigger(Triggers.chapter(1),
                SequenceEffect.of(new ScryEffect(2), new DrawCardEffect(1)));
        addTrigger(Triggers.chapter(2),
                SequenceEffect.of(new ScryEffect(2), new DrawCardEffect(1)));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "AvatarKuruk";
    }
}
