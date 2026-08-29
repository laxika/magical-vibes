package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageDrawEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "162")
public class SubiraTulzidiCaravanner extends Card {

    public SubiraTulzidiCaravanner() {
        PermanentPredicate powerAtMostTwoCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtMostPredicate(2)
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{1}: Another target creature with power 2 or less can't be blocked this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                powerAtMostTwoCreature,
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another creature with power 2 or less"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(
                        new DiscardHandCost(),
                        new RegisterDelayedCombatDamageDrawEffect(powerAtMostTwoCreature, false)
                ),
                "{1}{R}, {T}, Discard your hand: Until end of turn, whenever a creature you control "
                        + "with power 2 or less deals combat damage to a player, draw a card."
        ));
    }
}
