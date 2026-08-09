package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "163")
public class UmezawasJitte extends Card {

    public UmezawasJitte() {
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE,
                new PutCountersOnSelfEffect(CounterType.CHARGE, 2));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.CHARGE),
                        new BoostEquippedCreatureUntilEndOfTurnEffect(new Fixed(2), new Fixed(2))
                ),
                "Remove a charge counter from Umezawa's Jitte: Equipped creature gets +2/+2 until end of turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.CHARGE),
                        new BoostTargetCreatureEffect(-1, -1)
                ),
                "Remove a charge counter from Umezawa's Jitte: Target creature gets -1/-1 until end of turn.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.CHARGE),
                        new GainLifeEffect(2)
                ),
                "Remove a charge counter from Umezawa's Jitte: You gain 2 life."
        ));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
