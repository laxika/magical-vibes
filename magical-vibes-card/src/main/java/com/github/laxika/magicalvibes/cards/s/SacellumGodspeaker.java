package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.MatchingCardsInHand;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "146")
public class SacellumGodspeaker extends Card {

    public SacellumGodspeaker() {
        // {T}: Reveal any number of creature cards with power 5 or greater from your hand.
        // Add {G} for each card revealed this way. Revealing has no downside, so this is
        // modelled as the whole set of qualifying hand cards (cf. Phosphorescent Feast).
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(
                        ManaColor.GREEN,
                        new MatchingCardsInHand(CountScope.CONTROLLER, new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPowerAtLeastPredicate(5)
                        )))
                )),
                "{T}: Reveal any number of creature cards with power 5 or greater from your hand. "
                        + "Add {G} for each card revealed this way."
        ));
    }
}
