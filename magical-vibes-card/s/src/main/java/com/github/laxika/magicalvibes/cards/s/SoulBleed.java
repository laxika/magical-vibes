package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "113")
public class SoulBleed extends Card {

    public SoulBleed() {
        // At the beginning of the upkeep of enchanted creature's controller, that player loses 1 life.
        target(TargetFilters.creature()).addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PLAYER));
    }
}
