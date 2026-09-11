package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "184")
public class RickJonesDestinedSidekick extends Card {

    public RickJonesDestinedSidekick() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new MillControllerAndMayReturnMilledPermanentToHandEffect(
                        4,
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.HERO),
                                new CardTypePredicate(CardType.ENCHANTMENT))))),
                "{3}, {T}: Mill four cards. You may put a Hero or enchantment card from among those cards into your hand."
        ));
    }
}
