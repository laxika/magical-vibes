package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "153")
public class ShrineOfBurningRage extends Card {

    public ShrineOfBurningRage() {
        // At the beginning of your upkeep, put a charge counter on Shrine of Burning Rage.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.CHARGE));

        // Whenever you cast a red spell, put a charge counter on Shrine of Burning Rage.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.RED),
                        List.of(new PutCountersOnSelfEffect(CounterType.CHARGE))));

        // {3}, {T}, Sacrifice Shrine of Burning Rage: It deals damage equal to the number of
        // charge counters on it to any target.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new SacrificeSelfCost(),
                        new DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.CHARGE))
                ),
                "{3}, {T}, Sacrifice Shrine of Burning Rage: It deals damage equal to the number of charge counters on it to any target."
        ));
    }
}
