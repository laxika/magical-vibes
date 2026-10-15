package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "208")
public class ParadoxSurveyor extends Card {

    public ParadoxSurveyor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(5,
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.LAND),
                        new CardHasXInManaCostPredicate()))));
    }
}
