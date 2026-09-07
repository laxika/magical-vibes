package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "JUD", collectorNumber = "83")
public class BurningWish extends Card {

    public BurningWish() {
        addEffect(EffectSlot.SPELL, new SearchOutsideGameForCardToHandEffect(
                new CardTypePredicate(CardType.SORCERY)));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
