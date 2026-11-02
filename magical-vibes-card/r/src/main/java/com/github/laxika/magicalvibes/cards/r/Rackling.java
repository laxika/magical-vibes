package com.github.laxika.magicalvibes.cards.r;

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

@CardRegistration(set = "NEM", collectorNumber = "136")
public class Rackling extends Card {

    public Rackling() {
        // At the beginning of each opponent's upkeep, this creature deals X damage to that player,
        // where X is 3 minus the number of cards in their hand. X cannot be negative.
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                new DealDamageToPlayersEffect(
                        new Max(new Fixed(0),
                                new Sum(new Fixed(3), new Scaled(new CardsInHand(CountScope.TARGET_PLAYER), -1))),
                        DamageRecipient.ACTIVE_PLAYER));
    }
}
