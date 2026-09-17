package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "13")
@CardRegistration(set = "MUL", collectorNumber = "4")
@CardRegistration(set = "MUL", collectorNumber = "69")
@CardRegistration(set = "MUL", collectorNumber = "134")
@CardRegistration(set = "FCA", collectorNumber = "23")
public class KenrithTheReturnedKing extends Card {

    public KenrithTheReturnedKing() {
        // {R}: All creatures gain trample and haste until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new GrantKeywordEffect(
                        Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                        GrantScope.ALL_CREATURES_INCLUDING_SELF)),
                "{R}: All creatures gain trample and haste until end of turn."
        ));

        // {1}{G}: Put a +1/+1 counter on target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate())),
                "{1}{G}: Put a +1/+1 counter on target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(), "Target must be a creature")
        ));

        // {2}{W}: Target player gains 5 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new TargetPlayerGainsLifeEffect(5)),
                "{2}{W}: Target player gains 5 life.",
                anyPlayer()
        ));

        // {3}{U}: Target player draws a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new DrawCardForTargetPlayerEffect(1, false, true)),
                "{3}{U}: Target player draws a card.",
                anyPlayer()
        ));

        // {4}{B}: Put target creature card from a graveyard onto the battlefield under its owner's control.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .underOwnersControl(true)
                        .build()),
                "{4}{B}: Put target creature card from a graveyard onto the battlefield under its owner's control."
        ));
    }

    private static PlayerPredicateTargetFilter anyPlayer() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player");
    }
}
