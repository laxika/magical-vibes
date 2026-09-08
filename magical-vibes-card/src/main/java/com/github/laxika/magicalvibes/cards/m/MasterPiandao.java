package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "28")
public class MasterPiandao extends Card {

    public MasterPiandao() {
        addEffect(EffectSlot.ON_ATTACK, LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                4,
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.ALLY),
                        new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                        new CardSubtypePredicate(CardSubtype.LESSON)
                ))));
    }
}
