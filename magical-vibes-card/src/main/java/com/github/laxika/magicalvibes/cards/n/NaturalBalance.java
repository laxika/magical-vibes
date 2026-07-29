package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NaturalBalanceEffect;

@CardRegistration(set = "MIR", collectorNumber = "231")
public class NaturalBalance extends Card {

    public NaturalBalance() {
        // Land counts are read once, so the "sacrifice down to five" and "search up to five minus
        // your lands" halves apply to disjoint sets of players and cannot influence each other.
        addEffect(EffectSlot.SPELL, new NaturalBalanceEffect());
    }
}
