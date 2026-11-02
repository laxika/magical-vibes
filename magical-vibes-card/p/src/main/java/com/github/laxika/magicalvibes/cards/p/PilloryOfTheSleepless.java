package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "125")
@CardRegistration(set = "MM2", collectorNumber = "183")
@CardRegistration(set = "GK2", collectorNumber = "44")
@CardRegistration(set = "A25", collectorNumber = "213")
public class PilloryOfTheSleepless extends Card {

    public PilloryOfTheSleepless() {
        target(TargetFilters.creature())
                .addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect())
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                        new LoseLifeEffect(1)),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
