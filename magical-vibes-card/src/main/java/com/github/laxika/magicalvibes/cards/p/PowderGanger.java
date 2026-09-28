package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "65")
@CardRegistration(set = "PIP", collectorNumber = "391")
@CardRegistration(set = "PIP", collectorNumber = "593")
@CardRegistration(set = "PIP", collectorNumber = "919")
public class PowderGanger extends Card {

    public PowderGanger() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{2}")));
        target(TargetFilters.artifact(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenCopyOfSourceEffect(false, new RepeatedAdditionalCostCount("{2}")));
    }
}
