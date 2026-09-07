package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantPlayerStaticEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "240")
public class KayaGeistHunter extends Card {

    public KayaGeistHunter() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_CREATURES),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                "+1: Creatures you control gain deathtouch until end of turn. Put a +1/+1 counter on up to one target creature token you control.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTokenPredicate())),
                        "Target must be a creature token you control"),
                +1,
                null,
                null,
                List.of(),
                0,
                1));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new GrantPlayerStaticEffectsUntilEndOfTurnEffect(
                        List.of(new MultiplyTokenCreationEffect(2)))),
                "-2: Until end of turn, if one or more tokens would be created under your control, twice that many of those tokens are created instead."));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(
                        new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS),
                        new CreateTokenEffect(
                                new EventValue(),
                                "Spirit",
                                1,
                                1,
                                CardColor.WHITE,
                                List.of(CardSubtype.SPIRIT),
                                Set.of(Keyword.FLYING),
                                Set.of())),
                "-6: Exile all cards from all graveyards, then create a 1/1 white Spirit creature token with flying for each card exiled this way."));
    }
}
