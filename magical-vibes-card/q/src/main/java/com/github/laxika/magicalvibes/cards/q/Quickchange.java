package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "62")
public class Quickchange extends Card {

    public Quickchange() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BecomeChosenColorsUntilEndOfTurnEffect())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
