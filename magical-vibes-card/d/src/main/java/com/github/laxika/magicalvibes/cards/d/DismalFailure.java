package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;

@CardRegistration(set = "PLC", collectorNumber = "39")
public class DismalFailure extends Card {

    public DismalFailure() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new DiscardEffect(1, Subjects.controllerOf(Subjects.targetSpell())));
    }
}
