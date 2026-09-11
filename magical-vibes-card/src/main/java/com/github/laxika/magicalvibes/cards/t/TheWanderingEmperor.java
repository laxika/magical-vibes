package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.AllowLoyaltyActivationAtInstantSpeedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "42")
public class TheWanderingEmperor extends Card {

    public TheWanderingEmperor() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceEnteredBattlefieldThisTurn(),
                new AllowLoyaltyActivationAtInstantSpeedEffect()));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.TARGET)),
                "+1: Put a +1/+1 counter on up to one target creature. It gains first strike until end of turn.",
                null,
                +1,
                null,
                null,
                List.of(TargetFilters.creature()),
                0,
                1));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new CreateTokenEffect(
                        1,
                        "Samurai",
                        2,
                        2,
                        CardColor.WHITE,
                        List.of(CardSubtype.SAMURAI),
                        Set.of(Keyword.VIGILANCE),
                        Set.of())),
                "\u22121: Create a 2/2 white Samurai creature token with vigilance."));

        PermanentAllOfPredicate tappedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate()));
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new ExileTargetPermanentEffect(tappedCreature), new GainLifeEffect(2)),
                "\u22122: Exile target tapped creature. You gain 2 life.",
                new PermanentPredicateTargetFilter(tappedCreature, "Target must be a tapped creature")));
    }
}
