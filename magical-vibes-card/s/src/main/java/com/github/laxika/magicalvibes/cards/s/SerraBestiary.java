package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateTapAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "60")
@CardRegistration(set = "HML", collectorNumber = "15")
@CardRegistration(set = "ME4", collectorNumber = "27")
public class SerraBestiary extends Card {

    public SerraBestiary() {
        target(TargetFilters.creature())
                // At the beginning of your upkeep, sacrifice this Aura unless you pay {W}{W}.
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        new ForcedCostOrElseEffect(
                                new PayManaCost("{W}{W}"),
                                List.of(new SacrificeSelfEffect()),
                                true))
                // Enchanted creature can't attack or block, and its activated abilities with {T} in
                // their costs can't be activated.
                .addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect())
                .addStaticEffect(new EnchantedCreatureCantActivateTapAbilitiesEffect());
    }
}
