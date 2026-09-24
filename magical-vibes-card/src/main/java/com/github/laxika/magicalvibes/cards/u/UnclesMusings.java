package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "TLE", collectorNumber = "73")
public class UnclesMusings extends Card {

    public UnclesMusings() {
        addEffect(EffectSlot.SPELL, new ReturnCardsFromControllerGraveyardToHandEffect(
                new CardIsPermanentPredicate(), new XValue()));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
