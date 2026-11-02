package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "138")
public class WheelOfTorture extends Card {

    public WheelOfTorture() {
        // At the beginning of each opponent's upkeep, this artifact deals X damage to that player,
        // where X is 3 minus the number of cards in their hand. X cannot be negative.
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                new DealDamageToPlayersEffect(
                        new Max(new Fixed(0),
                                new Sum(new Fixed(3), new Scaled(new CardsInHand(CountScope.TARGET_PLAYER), -1))),
                        DamageRecipient.ACTIVE_PLAYER));
    }
}
