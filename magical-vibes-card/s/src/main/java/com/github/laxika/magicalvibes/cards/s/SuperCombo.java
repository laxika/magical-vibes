package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "34")
@CardRegistration(set = "TMC", collectorNumber = "95")
public class SuperCombo extends Card {

    public SuperCombo() {
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{2}")));
        addTrigger(Triggers.youCastThisSpell(), new ReplicateEffect("{2}"));

        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(new TargetDealsPowerDamageToTargetEffect());
    }
}
