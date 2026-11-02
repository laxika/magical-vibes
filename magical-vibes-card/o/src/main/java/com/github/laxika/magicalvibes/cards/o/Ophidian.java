package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "45")
@CardRegistration(set = "DD2", collectorNumber = "9")
@CardRegistration(set = "VMA", collectorNumber = "84")
@CardRegistration(set = "JVC", collectorNumber = "9")
public class Ophidian extends Card {

    public Ophidian() {
        // Whenever this creature attacks and isn't blocked, you may draw a card.
        // If you do, this creature assigns no combat damage this turn.
        addTrigger(Triggers.attacksUnblocked(),
                new MayEffect(SequenceEffect.of(
                        new DrawCardEffect(1),
                        new AssignNoCombatDamageEffect()),
                        "You may draw a card. If you do, this creature assigns no combat damage this turn."));
    }
}
