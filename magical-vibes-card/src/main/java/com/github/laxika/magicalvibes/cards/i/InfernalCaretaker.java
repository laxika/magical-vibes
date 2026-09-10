package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "76")
public class InfernalCaretaker extends Card {

    public InfernalCaretaker() {
        addMorph("{3}{B}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new EachPlayerReturnsCardsFromGraveyardToHandEffect(
                        Integer.MAX_VALUE, new CardSubtypePredicate(CardSubtype.ZOMBIE)));
    }
}
