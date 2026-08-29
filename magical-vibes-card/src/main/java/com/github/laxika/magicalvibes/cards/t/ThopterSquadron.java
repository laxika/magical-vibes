package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EXO", collectorNumber = "140")
@CardRegistration(set = "TPR", collectorNumber = "233")
public class ThopterSquadron extends Card {

    public ThopterSquadron() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new CreateTokenEffect("Thopter", 1, 1, null,
                                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING),
                                Set.of(CardType.ARTIFACT))
                ),
                "{1}, Remove a +1/+1 counter from Thopter Squadron: Create a 1/1 colorless "
                        + "Thopter artifact creature token with flying.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.THOPTER),
                                "Sacrifice another Thopter"),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "{1}, Sacrifice another Thopter: Put a +1/+1 counter on Thopter Squadron.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
