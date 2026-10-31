package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EmpowerNextCreatureSpellThisTurnEffect;

@CardRegistration(set = "TOR", collectorNumber = "127")
public class Insist extends Card {

    public Insist() {
        addSpellEffect(new EmpowerNextCreatureSpellThisTurnEffect(true, 0));
        addSpellEffect(new DrawCardEffect());
    }
}
