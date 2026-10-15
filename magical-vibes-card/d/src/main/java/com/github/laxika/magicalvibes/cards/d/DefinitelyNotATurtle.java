package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "98")
public class DefinitelyNotATurtle extends Card {

    public DefinitelyNotATurtle() {
        addEffect(EffectSlot.ON_DEATH,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        6,
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.LAND),
                                new CardAllOfPredicate(List.of(
                                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                                        new CardSubtypePredicate(CardSubtype.TURTLE)))))));
    }
}
