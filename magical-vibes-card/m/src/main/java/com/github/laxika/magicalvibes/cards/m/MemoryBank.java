package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BankEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MB1", collectorNumber = "27")
public class MemoryBank extends Card {

    public MemoryBank() {
        addSpellEffect(new DrawCardEffect());
        addSpellEffect(new BankEffect());
    }
}
