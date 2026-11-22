package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SHM", collectorNumber = "33")
@CardRegistration(set = "DPA", collectorNumber = "5")
public class Counterbore extends Card {

    public Counterbore() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(ExileCardsWithSameNameEffect.all(
                Subjects.targetSpell(), Subjects.controllerOf(Subjects.targetSpell())));
    }
}
