package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "INV", collectorNumber = "282")
@CardRegistration(set = "DDH", collectorNumber = "62")
@CardRegistration(set = "SCD", collectorNumber = "254")
public class Undermine extends Card {

    public Undermine() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new LoseLifeEffect(3, Subjects.controllerOf(Subjects.targetSpell())));
    }
}
