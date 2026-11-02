package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "111")
public class JackalopeHerd extends Card {

    public JackalopeHerd() {
        // When you cast a spell, return this creature to its owner's hand.
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(null, List.of(ReturnToHandEffect.self())));
    }
}
