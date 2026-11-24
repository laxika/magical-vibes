package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "736")
@CardRegistration(set = "CMM", collectorNumber = "766")
@CardRegistration(set = "ECC", collectorNumber = "93")
public class DescendantsFury extends Card {

    public DescendantsFury() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsCreaturePredicate()),
                TriggerOptions.ONE_OR_MORE,
                new MayEffect(SequenceEffect.of(
                        ChoosePermanentsEffect.exactly(1, Subjects.triggeringAll(), "one of them to sacrifice")
                                .toSacrifice(),
                        new SacrificeSelfEffect(Subjects.thisWay(StepKind.CHOOSE)),
                        new ConditionalEffect(HappenedThisWay.sacrificed(),
                                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                                        new CardAllOfPredicate(List.of(
                                                new CardTypePredicate(CardType.CREATURE),
                                                new CardSharesCreatureTypeWithPermanentsPredicate(
                                                        Subjects.departedThisWay(StepKind.SACRIFICE)))),
                                        LibrarySearchDestination.BATTLEFIELD))),
                        "Sacrifice one of them?"));
    }
}
