package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "AKH", collectorNumber = "112")
public class TrespassersCurse extends Card {

    public TrespassersCurse() {
        // Enchant player. Whenever a creature enchanted player controls enters, that player loses 1
        // life and you gain 1 life — a drain baked to the enchanted player by the enter collector.
        addEffect(EffectSlot.ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD,
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD,
                new GainLifeEffect(1));
    }
}
