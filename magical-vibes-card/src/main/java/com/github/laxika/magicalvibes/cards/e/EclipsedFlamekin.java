package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "219")
public class EclipsedFlamekin extends Card {

    public EclipsedFlamekin() {
        CardAnyOfPredicate elementalIslandOrMountain = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.ELEMENTAL),
                new CardSubtypePredicate(CardSubtype.ISLAND),
                new CardSubtypePredicate(CardSubtype.MOUNTAIN)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(4, elementalIslandOrMountain));
    }
}
