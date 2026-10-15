package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "1")
@CardRegistration(set = "40K", collectorNumber = "170")
@CardRegistration(set = "40K", collectorNumber = "177")
@CardRegistration(set = "40K", collectorNumber = "318")
public class SzarekhTheSilentKing extends Card {

    public SzarekhTheSilentKing() {
        CardAllOfPredicate artifactCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.ON_ATTACK, new MillControllerAndMayReturnMilledPermanentToHandEffect(
                3,
                new CardAnyOfPredicate(List.of(
                        artifactCreature,
                        new CardSubtypePredicate(CardSubtype.VEHICLE)))));
    }
}
