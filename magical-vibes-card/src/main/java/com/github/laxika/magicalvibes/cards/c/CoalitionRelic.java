package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FUT", collectorNumber = "161")
public class CoalitionRelic extends Card {

    public CoalitionRelic() {
        // {T}: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));

        // {T}: Put a charge counter on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCountersOnSelfEffect(CounterType.CHARGE)),
                "{T}: Put a charge counter on this artifact."
        ));

        // At the beginning of your first main phase, remove all charge counters from this artifact.
        // Each mana gets its own color choice, and EventValue preserves the number removed.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                SequenceEffect.of(
                        new RemoveAllCountersEffect(CounterType.CHARGE),
                        new AwardAnyColorManaEffect(new EventValue(), ManaSpendRestriction.NONE, null,
                                false, false, false, false, true, false, Set.of(), false)));
    }
}
