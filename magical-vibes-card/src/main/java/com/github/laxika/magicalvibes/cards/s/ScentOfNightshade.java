package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "UDS", collectorNumber = "69")
public class ScentOfNightshade extends Card {

    public ScentOfNightshade() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new RevealAnyNumberOfCardsFromHandEffect(
                        new CardColorPredicate(CardColor.BLACK)))
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                        new Scaled(new EventValue(), -1),
                        new Scaled(new EventValue(), -1)));
    }
}
