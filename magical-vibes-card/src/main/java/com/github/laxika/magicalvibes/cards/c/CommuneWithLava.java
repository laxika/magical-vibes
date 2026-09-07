package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

@CardRegistration(set = "DTK", collectorNumber = "131")
public class CommuneWithLava extends Card {

    public CommuneWithLava() {
        addEffect(EffectSlot.SPELL, new ExileTopCardsMayPlayUntilNextTurnEffect(new XValue()));
    }
}
