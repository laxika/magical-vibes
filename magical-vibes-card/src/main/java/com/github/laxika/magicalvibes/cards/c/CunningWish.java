package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "JUD", collectorNumber = "37")
public class CunningWish extends Card {

    public CunningWish() {
        addEffect(EffectSlot.SPELL, new SearchOutsideGameForCardToHandEffect(
                new CardTypePredicate(CardType.INSTANT)));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
