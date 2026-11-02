package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "127")
@CardRegistration(set = "SLD", collectorNumber = "2185")
public class IronMaiden extends Card {

    public IronMaiden() {
        // At the beginning of each opponent's upkeep, this artifact deals X damage to that player,
        // where X is the number of cards in their hand minus 4. X cannot be negative.
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT),
                new DealDamageToPlayersEffect(
                        new Max(new Fixed(0),
                                new Sum(new CardsInHand(CountScope.TARGET_PLAYER), new Fixed(-4))),
                        DamageRecipient.TARGET_PLAYER));
    }
}
