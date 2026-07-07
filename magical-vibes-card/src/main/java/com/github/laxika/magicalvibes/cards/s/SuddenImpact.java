package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "10E", collectorNumber = "241")
public class SuddenImpact extends Card {

    public SuddenImpact() {
        // Deals damage to target player equal to the number of cards in that player's hand.
        addEffect(EffectSlot.SPELL,
                new DealDamageToPlayersEffect(new CardsInHand(CountScope.TARGET_PLAYER), DamageRecipient.TARGET_PLAYER));
    }
}
