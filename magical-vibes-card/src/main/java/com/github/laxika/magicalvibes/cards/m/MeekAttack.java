package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerToughnessTotalAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "151")
@CardRegistration(set = "ECL", collectorNumber = "319")
@CardRegistration(set = "ECL", collectorNumber = "388")
@CardRegistration(set = "ECL", collectorNumber = "398")
public class MeekAttack extends Card {

    public MeekAttack() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardPowerToughnessTotalAtMostPredicate(5))),
                                "creature with total power and toughness 5 or less",
                                false, false, true, true),
                        "Put a creature card with total power and toughness 5 or less from your hand onto the battlefield?"
                )),
                "{1}{R}: You may put a creature card with total power and toughness 5 or less from your hand onto the battlefield. "
                        + "That creature gains haste. At the beginning of the next end step, sacrifice that creature."
        ));
    }
}
