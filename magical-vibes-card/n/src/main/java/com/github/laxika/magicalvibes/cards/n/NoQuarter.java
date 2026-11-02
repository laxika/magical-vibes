package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BlockParticipant;
import com.github.laxika.magicalvibes.model.effect.DestroyWeakerBlockParticipantEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "193")
public class NoQuarter extends Card {

    public NoQuarter() {
        // Whenever a creature becomes blocked by a creature with lesser power, destroy the blocking creature.
        addTrigger(Triggers.creatureBecomesBlocked(),
                new DestroyWeakerBlockParticipantEffect(BlockParticipant.BLOCKER));

        // Whenever a creature blocks a creature with lesser power, destroy the attacking creature.
        addTrigger(Triggers.creatureBecomesBlocked(),
                new DestroyWeakerBlockParticipantEffect(BlockParticipant.ATTACKER));
    }
}
