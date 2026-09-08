package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "23")
public class RecruitmentOfficer extends Card {

    public RecruitmentOfficer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(4,
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardMaxManaValuePredicate(3))))),
                "{3}{W}: Look at the top four cards of your library. You may reveal a creature card with "
                        + "mana value 3 or less from among them and put it into your hand. Put the rest on "
                        + "the bottom of your library in a random order."
        ));
    }
}
