package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "276")
public class SokenzanCrucibleOfDefiance extends Card {

    public SokenzanCrucibleOfDefiance() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(
                        new ReduceActivationCostEffect(new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)
                                )),
                                CountScope.CONTROLLER
                        )),
                        new CreateTokenEffect(
                                2,
                                "Spirit",
                                1,
                                1,
                                null,
                                null,
                                List.of(CardSubtype.SPIRIT),
                                Set.of(),
                                Set.of(Keyword.HASTE)
                        )
                ),
                "Channel — {3}{R}, Discard this card: Create two 1/1 colorless Spirit creature tokens. "
                        + "They gain haste until end of turn. This ability costs {1} less to activate for each "
                        + "legendary creature you control."
        ));
    }
}
