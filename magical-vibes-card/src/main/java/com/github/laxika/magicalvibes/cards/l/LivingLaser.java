package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsDiscardedOrCycledThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;

import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "55")
@CardRegistration(set = "MSC", collectorNumber = "366")
public class LivingLaser extends Card {

    public LivingLaser() {
        // Whenever Living Laser attacks, for each card you've discarded this turn, create a token
        // that's a copy of Living Laser, except the token isn't legendary. The tokens enter tapped
        // and attacking and are exiled at the beginning of the next end step.
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenCopyOfSourceEffect(
                true,
                new CardsDiscardedOrCycledThisTurn(),
                null,
                null,
                false,
                null,
                null,
                false,
                true,
                Map.of(),
                true,
                Set.of()
        ));
    }
}
