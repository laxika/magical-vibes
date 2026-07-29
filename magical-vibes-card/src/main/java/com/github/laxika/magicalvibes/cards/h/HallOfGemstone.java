package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllLandsProduceChosenColorUntilEndOfTurnEffect;

@CardRegistration(set = "MIR", collectorNumber = "221")
public class HallOfGemstone extends Card {

    public HallOfGemstone() {
        // EACH_UPKEEP_TRIGGERED puts the active player on the stack entry's targetId, so the handler
        // prompts the player whose upkeep it is — not this enchantment's controller.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new AllLandsProduceChosenColorUntilEndOfTurnEffect());
    }
}
