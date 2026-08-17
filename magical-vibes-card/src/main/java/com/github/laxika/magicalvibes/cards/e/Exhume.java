package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "USG", collectorNumber = "134")
@CardRegistration(set = "BRB", collectorNumber = "24")
public class Exhume extends Card {

    public Exhume() {
        addEffect(EffectSlot.SPELL,
                new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                        1,
                        new CardTypePredicate(CardType.CREATURE)));
    }
}
