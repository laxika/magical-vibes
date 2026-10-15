package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M19", collectorNumber = "247")
public class TransmogrifyingWand extends Card {

    public TransmogrifyingWand() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.CHARGE, new Fixed(3)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.CHARGE),
                        new DestroyTargetPermanentEffect(false, new CreateTokenEffect(
                                "Ox", 2, 4, CardColor.WHITE, List.of(CardSubtype.OX), Set.of(), Set.of()))
                ),
                "{1}, {T}, Remove a charge counter from this artifact: Destroy target creature. Its controller creates a 2/4 white Ox creature token. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
