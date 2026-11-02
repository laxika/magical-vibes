package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "119")
public class Withercrown extends Card {

    public Withercrown() {
        target(TargetFilters.creature())
                .addStaticEffect(new SetBasePowerToughnessEffect(0, null, GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                        ForcedCostOrElseEffect.enchantedControllerMayPay(
                                new SacrificePermanentCost(
                                        new PermanentIsHostOfSourceAuraPredicate(),
                                        "Sacrifice this creature", false),
                                List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER))));
    }
}
