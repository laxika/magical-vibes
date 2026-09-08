package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "211")
public class RowanScionOfWar extends Card {

    public RowanScionOfWar() {
        CardAnyOfPredicate blackOrRed = new CardAnyOfPredicate(List.of(
                new CardColorPredicate(CardColor.BLACK),
                new CardColorPredicate(CardColor.RED)));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(
                        blackOrRed, new LifeLostThisTurn(CountScope.CONTROLLER))),
                "{T}: Spells you cast this turn that are black and/or red cost {X} less to cast, "
                        + "where X is the amount of life you lost this turn. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
