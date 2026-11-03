package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouOrCreatureYouControlPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "278")
public class Reparations extends Card {

    public Reparations() {
        // Whenever an opponent casts a spell that targets you or a creature you control, you may draw a card.
        addTrigger(Triggers.opponentCastsSpell()
                        .withStackEntryFilter(new StackEntryTargetsYouOrCreatureYouControlPredicate()),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
