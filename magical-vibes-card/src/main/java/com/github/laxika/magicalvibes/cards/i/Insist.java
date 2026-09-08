package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EmpowerNextCreatureSpellThisTurnEffect;

@CardRegistration(set = "TOR", collectorNumber = "127")
public class Insist extends Card {

    public Insist() {
        addEffect(EffectSlot.SPELL, new EmpowerNextCreatureSpellThisTurnEffect(true, 0));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
