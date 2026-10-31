package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "JUD", collectorNumber = "83")
@CardRegistration(set = "VMA", collectorNumber = "152")
public class BurningWish extends Card {

    public BurningWish() {
        addSpellEffect(new SearchOutsideGameForCardToHandEffect(
                new CardTypePredicate(CardType.SORCERY)));
        addSpellEffect(new ExileSpellEffect());
    }
}
