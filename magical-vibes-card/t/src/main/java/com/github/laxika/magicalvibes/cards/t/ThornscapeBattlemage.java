package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "94")
@CardRegistration(set = "TSB", collectorNumber = "87")
@CardRegistration(set = "DDE", collectorNumber = "42")
@CardRegistration(set = "MM3", collectorNumber = "142")
public class ThornscapeBattlemage extends Card {

    public ThornscapeBattlemage() {
        addStaticEffect(new KickerEffect("{R}"));
        addCastingCost(RepeatableAdditionalManaCost.singlePayment(List.of("{W}")));

        targetWhenKicked(null, 0, 0, 1, 1).addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                new DealDamageToAnyTargetEffect(2));

        targetWithDynamicCount(new RepeatedAdditionalCostCount("{W}"), TargetFilters.artifact(), 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        TriggerMode.INDEPENDENT,
                        TriggerOptions.DEFAULT.withInterveningIf(new RepeatedAdditionalCostPaid("{W}")),
                        new DestroyTargetPermanentEffect());
    }
}
