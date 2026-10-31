package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "JUD", collectorNumber = "37")
@CardRegistration(set = "MB2", collectorNumber = "159")
public class CunningWish extends Card {

    public CunningWish() {
        addSpellEffect(new SearchOutsideGameForCardToHandEffect(
                new CardTypePredicate(CardType.INSTANT)));
        addSpellEffect(new ExileSpellEffect());
    }
}
