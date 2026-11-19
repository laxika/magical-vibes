package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ONE", collectorNumber = "65")
public class PrologueToPhyresis extends Card {

    public PrologueToPhyresis() {
        addSpellEffect(new GivePoisonCountersEffect(1, Subjects.eachOpponent()));
        addSpellEffect(new DrawCardEffect(1));
    }
}
