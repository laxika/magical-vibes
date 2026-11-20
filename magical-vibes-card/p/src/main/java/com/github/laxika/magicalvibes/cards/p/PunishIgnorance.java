package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "ALA", collectorNumber = "183")
public class PunishIgnorance extends Card {

    public PunishIgnorance() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new LoseLifeEffect(3, Subjects.controllerOf(Subjects.targetSpell())));
        addSpellEffect(new GainLifeEffect(3));
    }
}
