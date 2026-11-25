package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "151")
@CardRegistration(set = "MKM", collectorNumber = "408")
public class ArchdruidsCharm extends Card {

    public ArchdruidsCharm() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Search your library for a creature or land card and reveal it. Put it onto the battlefield tapped if it's a land card. Otherwise, put it into your hand. Then shuffle",
                        List.of(
                                new SearchLibraryEffect(new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardTypePredicate(CardType.LAND))),
                                        LibrarySearchDestination.REVEAL_ONLY, null, false),
                                new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.SEARCH,
                                        new CardTypePredicate(CardType.LAND))).enteringTapped(),
                                new PutCardsIntoHandEffect(Subjects.foundThisWay()),
                                new ShuffleLibraryEffect(false))),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on target creature you control. It deals damage equal to its power to target creature you don't control",
                        List.of(
                                new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                new TargetDealsPowerDamageToTargetEffect()),
                        List.of(
                                new ControlledPermanentPredicateTargetFilter(
                                        new PermanentIsCreaturePredicate(),
                                        "First target must be a creature you control."),
                                new PermanentPredicateTargetFilter(
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentNotPredicate(
                                                        new PermanentControlledBySourceControllerPredicate()))),
                                        "Second target must be a creature you don't control."))),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target artifact or enchantment",
                        new ExileTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsEnchantmentPredicate())),
                                "Target must be an artifact or enchantment."))
        )));
    }
}
