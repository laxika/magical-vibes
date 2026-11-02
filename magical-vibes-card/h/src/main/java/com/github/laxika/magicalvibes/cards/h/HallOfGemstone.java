package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllLandsProduceChosenColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "221")
public class HallOfGemstone extends Card {

    public HallOfGemstone() {
        // Triggers.upkeep(ActivePlayer.ANY) puts the active player on the stack entry's targetId, so the handler
        // prompts the player whose upkeep it is — not this enchantment's controller.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new AllLandsProduceChosenColorUntilEndOfTurnEffect());
    }
}
