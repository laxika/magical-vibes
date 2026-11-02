package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EnchantedPermanentManaValue;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "23")
public class SoulTithe extends Card {

    public SoulTithe() {
        // Enchant nonland permanent. At the beginning of the upkeep of enchanted permanent's
        // controller, that player sacrifices it unless they pay {X}, where X is its mana value.
        target(TargetFilters.nonlandPermanent()).addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                ForcedCostOrElseEffect.enchantedControllerMayPay(
                        PayManaCost.withGenericIncrease("{0}", new EnchantedPermanentManaValue()),
                        List.of(new SacrificeEnchantedCreatureEffect())));
    }
}
