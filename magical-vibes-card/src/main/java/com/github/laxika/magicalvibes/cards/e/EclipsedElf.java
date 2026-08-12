package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "218")
@CardRegistration(set = "ECL", collectorNumber = "336")
public class EclipsedElf extends Card {

    public EclipsedElf() {
        CardAnyOfPredicate elfSwampOrForest = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.ELF),
                new CardSubtypePredicate(CardSubtype.SWAMP),
                new CardSubtypePredicate(CardSubtype.FOREST)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(4, elfSwampOrForest));
    }
}
