package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "78")
@CardRegistration(set = "M15", collectorNumber = "116")
@CardRegistration(set = "DDM", collectorNumber = "72")
@CardRegistration(set = "GK2", collectorNumber = "35")
@CardRegistration(set = "RVR", collectorNumber = "93")
@CardRegistration(set = "WOT", collectorNumber = "36")
@CardRegistration(set = "PIO", collectorNumber = "111")
public class StabWound extends Card {

    public StabWound() {
        target(TargetFilters.creature())
                // Enchanted creature gets -2/-2.
                .addStaticEffect(new StaticBoostEffect(-2, -2, GrantScope.ENCHANTED_CREATURE))

                // At the beginning of the upkeep of enchanted creature's controller,
                // that player loses 2 life.
                .addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                        new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER));
    }
}
