package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsThisTurnEffect;

@CardRegistration(set = "M11", collectorNumber = "30")
public class Silence extends Card {

    public Silence() {
        addEffect(EffectSlot.SPELL, new OpponentsCantCastSpellsThisTurnEffect());
    }
}
