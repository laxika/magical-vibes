package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "13")
@CardRegistration(set = "DDH", collectorNumber = "26")
public class RecumbentBliss extends Card {

    public RecumbentBliss() {
        // Enchant creature. Enchanted creature can't attack or block.
        target(TargetFilters.creature())
                .addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect());

        // At the beginning of your upkeep, you may gain 1 life.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}
