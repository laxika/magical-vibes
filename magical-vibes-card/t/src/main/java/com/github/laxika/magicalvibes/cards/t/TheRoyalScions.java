package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.StepKind;

@CardRegistration(set = "ELD", collectorNumber = "199")
@CardRegistration(set = "ELD", collectorNumber = "272")
@CardRegistration(set = "SLD", collectorNumber = "1600")
public class TheRoyalScions extends Card {

    public TheRoyalScions() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(1), new DiscardEffect(1, Subjects.you())),
                "+1: Draw a card, then discard a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(
                        new BoostTargetCreatureEffect(2, 0),
                        new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.TRAMPLE), Subjects.target())
                ),
                "+1: Target creature gets +2/+0 and gains first strike and trample until end of turn.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new DrawCardEffect(4),
                        new ConditionalEffect(HappenedThisWay.atLeast(StepKind.DRAW, 4),
                                new QueueReflexiveAbilityEffect(
                                        new DealDamageToAnyTargetEffect(new CardsInHand(CountScope.CONTROLLER))))),
                "\u22128: Draw four cards. When you do, The Royal Scions deals damage to any target equal to the number of cards in your hand."
        ));
    }
}
