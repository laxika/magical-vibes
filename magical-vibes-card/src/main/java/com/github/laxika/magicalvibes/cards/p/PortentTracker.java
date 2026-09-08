package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentProtectedByOpponentOfSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "201")
public class PortentTracker extends Card {

    public PortentTracker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET)),
                "{T}: Untap target land.",
                TargetFilters.land()));

        PermanentPredicate protectedByOpponent = new PermanentProtectedByOpponentOfSourceControllerPredicate();
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ConditionalEffect(
                                new TargetPermanentMatches(protectedByOpponent),
                                new RemoveCounterFromTargetPermanentEffect(CounterType.DEFENSE, null, 1)),
                        new ConditionalEffect(
                                new NotCondition(new TargetPermanentMatches(protectedByOpponent)),
                                new PutCounterOnTargetPermanentEffect(CounterType.DEFENSE, 1))),
                "{T}: Choose target battle. If an opponent protects it, remove a defense counter from it. Otherwise, put a defense counter on it. Activate only as a sorcery.",
                new PermanentPredicateTargetFilter(new PermanentIsBattlePredicate(), "Target must be a battle"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
