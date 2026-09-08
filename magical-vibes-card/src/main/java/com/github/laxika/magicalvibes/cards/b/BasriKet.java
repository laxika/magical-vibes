package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedNontokenAttackTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "7")
public class BasriKet extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of combat on your turn, create a 1/1 white Soldier creature token, then put a +1/+1 counter on each creature you control.";
    private static final CreateTokenEffect SOLDIER_TOKEN = new CreateTokenEffect(
            1, "Soldier", 1, 1, CardColor.WHITE, List.of(CardSubtype.SOLDIER), true);

    public BasriKet() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.TARGET,
                                new PermanentIsCreaturePredicate())
                ),
                "+1: Put a +1/+1 counter on up to one target creature. It gains indestructible until end of turn.",
                null, +1, null, null,
                List.<TargetFilter>of(TargetFilters.creature()), 0, 1));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new RegisterDelayedNontokenAttackTokenEffect(SOLDIER_TOKEN)),
                "−2: Whenever one or more nontoken creatures attack this turn, create that many 1/1 white Soldier creature tokens that are tapped and attacking."
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.BEGINNING_OF_COMBAT,
                                List.of(
                                        CreateTokenEffect.whiteSoldier(1),
                                        new PutCounterOnEachControlledPermanentEffect(
                                                CounterType.PLUS_ONE_PLUS_ONE, 1,
                                                new PermanentIsCreaturePredicate())
                                ),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "−6: You get an emblem with \"" + EMBLEM_TEXT + "\""
        ));
    }
}
