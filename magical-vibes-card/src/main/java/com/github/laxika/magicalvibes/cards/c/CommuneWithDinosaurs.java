package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "181")
public class CommuneWithDinosaurs extends Card {

    public CommuneWithDinosaurs() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(5,
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.DINOSAUR),
                        new CardTypePredicate(CardType.LAND)
                ))
        ));
    }
}
