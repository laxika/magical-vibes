package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

@CardRegistration(set = "DTK", collectorNumber = "131")
@CardRegistration(set = "AFC", collectorNumber = "118")
@CardRegistration(set = "C20", collectorNumber = "149")
public class CommuneWithLava extends Card {

    public CommuneWithLava() {
        addSpellEffect(new ExileTopCardsMayPlayUntilNextTurnEffect(new XValue()));
    }
}
