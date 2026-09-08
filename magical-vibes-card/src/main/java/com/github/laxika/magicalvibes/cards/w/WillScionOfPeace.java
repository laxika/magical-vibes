package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "218")
public class WillScionOfPeace extends Card {

    public WillScionOfPeace() {
        CardAnyOfPredicate whiteOrBlue = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(CardColor.WHITE),
                new CardColorPredicate(CardColor.BLUE)));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(
                        whiteOrBlue, new LifeGainedThisTurn(CountScope.CONTROLLER))),
                "{T}: Spells you cast this turn that are white and/or blue cost {X} less to cast, "
                        + "where X is the amount of life you gained this turn. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
