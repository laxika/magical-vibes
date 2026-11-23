package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "142")
public class SkullknockerOgre extends Card {

    public SkullknockerOgre() {
        addTrigger(Triggers.dealsDamageToOpponent(),
                SequenceEffect.of(new DiscardEffect(1, Subjects.targetPlayer(), true),
                        new ConditionalEffect(HappenedThisWay.discarded(),
                                new DrawCardEffect(1, Subjects.targetPlayer()))));
    }
}
