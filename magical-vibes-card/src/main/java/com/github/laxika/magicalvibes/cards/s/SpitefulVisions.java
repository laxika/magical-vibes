package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;

@CardRegistration(set = "SHM", collectorNumber = "198")
public class SpitefulVisions extends Card {

    public SpitefulVisions() {
        // At the beginning of each player's draw step, that player draws an additional card.
        addEffect(EffectSlot.EACH_DRAW_TRIGGERED, new DrawCardForTargetPlayerEffect(1));

        // Whenever a player draws a card, this enchantment deals 1 damage to that player.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_OPPONENT_DRAWS, new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER));
    }
}
