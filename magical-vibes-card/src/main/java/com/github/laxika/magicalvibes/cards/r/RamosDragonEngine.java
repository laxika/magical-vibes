package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellColorCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "BRR", collectorNumber = "47")
public class RamosDragonEngine extends Card {

    public RamosDragonEngine() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null,
                        List.of(new PutCountersOnSelfEffect(
                                CounterType.PLUS_ONE_PLUS_ONE,
                                new TriggeringSpellColorCount()))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(5, CounterType.PLUS_ONE_PLUS_ONE),
                        new AwardManaEffect(ManaColor.WHITE, 2),
                        new AwardManaEffect(ManaColor.BLUE, 2),
                        new AwardManaEffect(ManaColor.BLACK, 2),
                        new AwardManaEffect(ManaColor.RED, 2),
                        new AwardManaEffect(ManaColor.GREEN, 2)
                ),
                "Remove five +1/+1 counters from Ramos: Add {W}{W}{U}{U}{B}{B}{R}{R}{G}{G}. "
                        + "Activate only once each turn.",
                1
        ));
    }
}
