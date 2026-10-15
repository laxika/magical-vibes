package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "320")
public class BasriDevotedPaladin extends Card {

    public BasriDevotedPaladin() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.TARGET,
                                new PermanentIsCreaturePredicate())
                ),
                "+1: Put a +1/+1 counter on up to one target creature. It gains vigilance until end of turn.",
                null, +1, null, null,
                List.<TargetFilter>of(TargetFilters.creature()), 0, 1));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                        EffectSlot.ON_ANY_CREATURE_ATTACKS,
                        new PutCountersOnSourceEffect(1, 1, 1))),
                "−1: Whenever a creature attacks this turn, put a +1/+1 counter on it."));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(
                        new BoostAllOwnCreaturesEffect(2, 2),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.OWN_CREATURES)
                ),
                "−6: Creatures you control get +2/+2 and gain flying until end of turn."));
    }
}
