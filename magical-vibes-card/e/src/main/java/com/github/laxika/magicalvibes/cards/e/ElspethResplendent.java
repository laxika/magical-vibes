package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "11")
public class ElspethResplendent extends Card {

    public ElspethResplendent() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new ChooseOneForTargetCreatureEffect(List.of(
                        counterMode("Flying", CounterType.FLYING),
                        counterMode("First strike", CounterType.FIRST_STRIKE),
                        counterMode("Lifelink", CounterType.LIFELINK),
                        counterMode("Vigilance", CounterType.VIGILANCE)
                ))),
                "+1: Choose up to one target creature. Put a +1/+1 counter and a counter from among flying, first strike, lifelink, or vigilance on it.",
                null, +1, null, null,
                List.<TargetFilter>of(TargetFilters.creature()), 0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(
                        7,
                        new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardMaxManaValuePredicate(3)
                        )),
                        new Fixed(3),
                        new EnterWithCountersEffect(CounterType.SHIELD, new Fixed(1))
                )),
                "−3: Look at the top seven cards of your library. You may put a permanent card with mana value 3 or less from among them onto the battlefield with a shield counter on it. Put the rest on the bottom of your library in a random order."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateTokenEffect(
                        5, "Angel", 3, 3, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), Set.of()
                )),
                "−7: Create five 3/3 white Angel creature tokens with flying."
        ));
    }

    private static ChooseOneEffect.ChooseOneOption counterMode(String label, CounterType counterType) {
        return new ChooseOneEffect.ChooseOneOption(label, List.of(
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new PutCounterOnTargetPermanentEffect(counterType)
        ));
    }
}
