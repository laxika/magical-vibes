package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "212")
public class HaraldKingOfSkemfar extends Card {

    public HaraldKingOfSkemfar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(5,
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.ELF),
                                new CardSubtypePredicate(CardSubtype.WARRIOR),
                                new CardSubtypePredicate(CardSubtype.TYVAR)
                        ))));
    }
}
