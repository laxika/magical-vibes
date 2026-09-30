package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenPerpetuallyBoostGraveyardAndMayReturnMilledCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "19")
public class ElvishElegy extends Card {

    public ElvishElegy() {
        addEffect(EffectSlot.SPELL,
                new MillControllerThenPerpetuallyBoostGraveyardAndMayReturnMilledCardEffect(
                        3,
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.ELF),
                                new CardTypePredicate(CardType.LAND))),
                        1,
                        1));
    }
}
