package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "177")
public class BattleStrain extends Card {

    public BattleStrain() {
        // The blocking creature is baked onto the trigger as its non-targeting target, so
        // TARGET_PERMANENT_CONTROLLER is "that creature's controller".
        addTrigger(Triggers.creatureBlocks(),
                new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
