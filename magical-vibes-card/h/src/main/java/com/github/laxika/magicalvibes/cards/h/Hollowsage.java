package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SHM", collectorNumber = "69")
public class Hollowsage extends Card {

    public Hollowsage() {
        // Whenever this creature becomes untapped, you may have target player discard a card.
        // The "may" and the (any) player target are resolved on the stack via the MayEffect flow.
        addTrigger(Triggers.becomesUntapped(),
                new MayEffect(new DiscardEffect(1, Subjects.targetPlayer()),
                        "have target player discard a card"));
    }
}
