package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "137")
public class FalkenrathPitFighter extends Card {

    public FalkenrathPitFighter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE)
                                )),
                                "Sacrifice a Vampire",
                                false
                        ),
                        new DrawCardEffect(2)
                ),
                "{1}{R}, Discard a card, Sacrifice a Vampire: Draw two cards. Activate only if an opponent lost life this turn."
        ).withActivationCondition(
                new OpponentLostLifeThisTurn(1),
                "Activate only if an opponent lost life this turn"
        ));
    }
}
