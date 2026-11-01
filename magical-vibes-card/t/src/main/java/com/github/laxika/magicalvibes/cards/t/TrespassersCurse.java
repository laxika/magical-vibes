package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "112")
@CardRegistration(set = "AKR", collectorNumber = "129")
public class TrespassersCurse extends Card {

    public TrespassersCurse() {
        // Enchant player. Whenever a creature enchanted player controls enters, that player loses 1
        // life and you gain 1 life — a drain baked to the enchanted player by the enter collector.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ENCHANTED_PLAYER_CONTROLS),
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
                new GainLifeEffect(1));
    }
}
