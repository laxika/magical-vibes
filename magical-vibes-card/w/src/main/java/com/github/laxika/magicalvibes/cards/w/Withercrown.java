package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "119")
public class Withercrown extends Card {

    public Withercrown() {
        target(TargetFilters.creature())
                .addStaticEffect(new SetBasePowerToughnessEffect(0, null, GrantScope.ENCHANTED_CREATURE))
                // Enchanted creature has "At the beginning of your upkeep, you lose 1 life unless you sacrifice
                // this creature."
                .addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(
                        Triggers.upkeep(ActivePlayer.YOU),
                        new ForcedCostOrElseEffect(
                                new SacrificePermanentCost(new PermanentIsSourcePermanentPredicate(),
                                        "Sacrifice this creature", false),
                                List.of(new LoseLifeEffect(1, LoseLifeRecipient.CONTROLLER)),
                                true)),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
