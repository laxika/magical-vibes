package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "13")
public class KenrithTheReturnedKing extends Card {

    public KenrithTheReturnedKing() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                        GrantScope.ALL_CREATURES_INCLUDING_SELF)),
                "{R}: All creatures gain trample and haste until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                "{1}{G}: Put a +1/+1 counter on target creature.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new TargetPlayerGainsLifeEffect(5)),
                "{2}{W}: Target player gains 5 life.",
                anyPlayerTarget()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new DrawCardForTargetPlayerEffect(1, false, true)),
                "{3}{U}: Target player draws a card.",
                anyPlayerTarget()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .targetGraveyard(true)
                        .underOwnersControl(true)
                        .build()),
                "{4}{B}: Put target creature card from a graveyard onto the battlefield under its owner's control."
        ));
    }

    private static PlayerPredicateTargetFilter anyPlayerTarget() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        );
    }
}
