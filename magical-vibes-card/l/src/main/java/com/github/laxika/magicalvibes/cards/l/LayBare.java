package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ROE", collectorNumber = "74")
public class LayBare extends Card {

    public LayBare() {
        addSpellEffect(new LookAtHandEffect(Subjects.controllerOf(Subjects.targetSpell())));
        addSpellEffect(new CounterSpellEffect());
    }
}
