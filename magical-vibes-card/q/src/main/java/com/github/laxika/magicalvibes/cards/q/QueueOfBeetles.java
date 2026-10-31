package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StackUsesFirstInFirstOutEffect;

@CardRegistration(set = "MB1", collectorNumber = "61")
public class QueueOfBeetles extends Card {

    public QueueOfBeetles() {
        addStaticEffect(new StackUsesFirstInFirstOutEffect());
    }
}
