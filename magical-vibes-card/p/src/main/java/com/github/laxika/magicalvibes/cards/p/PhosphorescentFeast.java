package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsInRevealedCards;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "EVE", collectorNumber = "72")
@CardRegistration(set = "FUT", collectorNumber = "149")
public class PhosphorescentFeast extends Card {

    public PhosphorescentFeast() {
        addEffect(EffectSlot.SPELL, new RevealAnyNumberOfCardsFromHandEffect(new CardTruePredicate()));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(
                new Scaled(new ColorManaSymbolsInRevealedCards(ManaColor.GREEN), 2)));
    }
}
