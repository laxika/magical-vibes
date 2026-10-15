package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "200")
public class NissaGenesisMage extends Card {

    public NissaGenesisMage() {
        PermanentPredicateTargetFilter creatureOrLand = new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate())),
                "Target must be a creature or land");

        // +2: Untap up to two target creatures and up to two target lands.
        // Four identical creature-or-land slots; AT_MOST_TWO_CREATURES_AND_TWO_LANDS caps each type.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS)),
                "+2: Untap up to two target creatures and up to two target lands.",
                null, +2, null, null,
                List.<TargetFilter>of(creatureOrLand, creatureOrLand, creatureOrLand, creatureOrLand),
                0, 4
        ).withMultiTargetConstraint(MultiTargetConstraint.AT_MOST_TWO_CREATURES_AND_TWO_LANDS));

        // −3: Target creature gets +5/+5 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new BoostTargetCreatureEffect(5, 5)),
                "−3: Target creature gets +5/+5 until end of turn.",
                TargetFilters.creature()
        ));

        // −10: Look at the top ten cards of your library. You may put any number of creature and/or
        //      land cards from among them onto the battlefield. Put the rest on the bottom of your
        //      library in a random order.
        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(LookAtTopCardsEffect.mayPutAnyNumberMatchingOntoBattlefieldRestOnBottomRandom(
                        10,
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.LAND))))),
                "−10: Look at the top ten cards of your library. You may put any number of creature "
                        + "and/or land cards from among them onto the battlefield. Put the rest on "
                        + "the bottom of your library in a random order."
        ));
    }
}
