package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostOwnedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "8")
public class NetworkMarauder extends Card {

    private static final CardAllOfPredicate ARTIFACT_CREATURE = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.ARTIFACT),
            new CardTypePredicate(CardType.CREATURE)));

    private static final CardAnyOfPredicate BOOSTED_CARDS = new CardAnyOfPredicate(List.of(
            ARTIFACT_CREATURE,
            new CardSubtypePredicate(CardSubtype.SPACECRAFT)));

    private static final PerpetuallyBoostOwnedCardsEffect BOOST =
            new PerpetuallyBoostOwnedCardsEffect(BOOSTED_CARDS, 1, 1);

    public NetworkMarauder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, BOOST);
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardMinManaValuePredicate(3))),
                        BOOST));
    }
}
