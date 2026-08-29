package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "BNG", collectorNumber = "68")
public class FateUnraveler extends Card {

    public FateUnraveler() {
        // Whenever an opponent draws a card, this creature deals 1 damage to that player.
        addEffect(EffectSlot.ON_OPPONENT_DRAWS, new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER));
    }
}
