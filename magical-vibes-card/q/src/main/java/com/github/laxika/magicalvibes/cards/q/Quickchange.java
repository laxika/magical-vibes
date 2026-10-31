package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "62")
public class Quickchange extends Card {

    public Quickchange() {
        target(TargetFilters.creature())
                .addSpellEffect(new BecomeChosenColorsUntilEndOfTurnEffect())
                .addSpellEffect(new DrawCardEffect());
    }
}
