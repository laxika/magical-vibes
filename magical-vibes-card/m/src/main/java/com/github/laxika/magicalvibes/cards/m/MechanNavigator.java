package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "EOE", collectorNumber = "64")
public class MechanNavigator extends Card {

    public MechanNavigator() {
        addTrigger(Triggers.becomesTapped(),
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, Subjects.you())));
    }
}
