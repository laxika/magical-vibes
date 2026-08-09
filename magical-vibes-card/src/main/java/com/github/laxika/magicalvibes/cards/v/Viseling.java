package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "NEM", collectorNumber = "140")
public class Viseling extends Card {

    public Viseling() {
        // At the beginning of each opponent's upkeep, this creature deals X damage to that player,
        // where X is the number of cards in their hand minus 4. X cannot be negative.
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(
                        new Max(new Fixed(0),
                                new Sum(new CardsInHand(CountScope.TARGET_PLAYER), new Fixed(-4))),
                        DamageRecipient.TARGET_PLAYER));
    }
}
