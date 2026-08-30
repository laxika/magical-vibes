package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

@CardRegistration(set = "FUT", collectorNumber = "156")
public class GlitteringWish extends Card {

    public GlitteringWish() {
        addEffect(EffectSlot.SPELL, new SearchOutsideGameForCardToHandEffect(
                new CardIsMulticoloredPredicate()));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
