package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersHaveNoMaximumHandSizeEffect;

@CardRegistration(set = "C13", collectorNumber = "89")
public class PriceOfKnowledge extends Card {

    public PriceOfKnowledge() {
        // Players have no maximum hand size.
        addEffect(EffectSlot.STATIC, new PlayersHaveNoMaximumHandSizeEffect());

        // At the beginning of each opponent's upkeep, this enchantment deals damage to that
        // player equal to the number of cards in that player's hand.
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(new CardsInHand(CountScope.TARGET_PLAYER),
                        DamageRecipient.TARGET_PLAYER));
    }
}
