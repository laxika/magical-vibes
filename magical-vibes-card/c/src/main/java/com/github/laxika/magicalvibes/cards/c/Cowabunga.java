package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "113")
public class Cowabunga extends Card {

    public Cowabunga() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                4, new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.MUTANT),
                        new CardSubtypePredicate(CardSubtype.NINJA),
                        new CardSubtypePredicate(CardSubtype.TURTLE),
                        new CardTypePredicate(CardType.LAND)))));
    }
}
