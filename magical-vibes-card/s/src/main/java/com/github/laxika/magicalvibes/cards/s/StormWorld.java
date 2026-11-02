package com.github.laxika.magicalvibes.cards.s;

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

@CardRegistration(set = "LEG", collectorNumber = "165")
@CardRegistration(set = "ME3", collectorNumber = "111")
public class StormWorld extends Card {

    public StormWorld() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new DealDamageToPlayersEffect(
                        new Max(new Fixed(0), new Sum(
                                new Fixed(4),
                                new Scaled(new CardsInHand(CountScope.TARGET_PLAYER), -1))),
                        DamageRecipient.ACTIVE_PLAYER));
    }
}
