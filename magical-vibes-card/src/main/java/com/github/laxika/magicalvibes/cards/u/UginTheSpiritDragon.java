package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValueXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "1")
public class UginTheSpiritDragon extends Card {

    public UginTheSpiritDragon() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new DealDamageToAnyTargetEffect(3)),
                "+2: Ugin deals 3 damage to any target."
        ));

        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(new ExileAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsColorlessPredicate()),
                        new PermanentMaxManaValueXPredicate()
                )))),
                "\u2212X: Exile each permanent with mana value X or less that's one or more colors.",
                null
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(
                        new GainLifeEffect(7),
                        new DrawCardEffect(7),
                        new PutUpToCardsFromHandOntoBattlefieldEffect(
                                new CardIsPermanentPredicate(), "permanent", 7)
                ),
                "\u221210: You gain 7 life, draw seven cards, then put up to seven permanent cards from your hand "
                        + "onto the battlefield."
        ));
    }
}
