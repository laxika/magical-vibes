package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "50")
@CardRegistration(set = "CMD", collectorNumber = "50")
@CardRegistration(set = "CM2", collectorNumber = "45")
public class MemoryErosion extends Card {

    public MemoryErosion() {
        // Whenever an opponent casts a spell, that player mills two cards.
        addTrigger(Triggers.opponentCastsSpell(), new MillEffect(2, MillRecipient.TARGET_PLAYER));
    }
}
