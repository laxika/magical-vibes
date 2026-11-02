package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.SkipRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "9ED", collectorNumber = "7")
@CardRegistration(set = "8ED", collectorNumber = "8")
@CardRegistration(set = "NEM", collectorNumber = "3")
@CardRegistration(set = "SLD", collectorNumber = "1379")
public class BlindingAngel extends Card {

    public BlindingAngel() {
        // Whenever this creature deals combat damage to a player, that player skips their next combat phase.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new SkipNextEffect(SkipKind.COMBAT_PHASE, SkipRecipient.DAMAGED_PLAYER));
    }
}
