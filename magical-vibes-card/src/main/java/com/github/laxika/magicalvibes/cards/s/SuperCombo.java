package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "34")
@CardRegistration(set = "TMC", collectorNumber = "95")
public class SuperCombo extends Card {

    public SuperCombo() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(List.of("{2}")));
        addEffect(EffectSlot.ON_SELF_CAST, new ReplicateEffect("{2}"));

        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.SPELL, new TargetDealsPowerDamageToTargetEffect());
    }
}
