package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "183")
@CardRegistration(set = "GK2", collectorNumber = "41")
public class OneThousandLashes extends Card {

    public OneThousandLashes() {
        target(TargetFilters.creature())
                // Enchanted creature can't attack or block, and its activated abilities
                // can't be activated.
                .addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect())
                .addStaticEffect(new EnchantedCreatureCantActivateAbilitiesEffect())

                // At the beginning of the upkeep of enchanted creature's controller,
                // that player loses 1 life.
                .addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                        new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PLAYER));
    }
}
