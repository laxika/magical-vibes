package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "LEG", collectorNumber = "165")
public class StormWorld extends Card {

    public StormWorld() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(
                        new Max(new Fixed(0), new Sum(
                                new Fixed(4),
                                new Scaled(new CardsInHand(CountScope.TARGET_PLAYER), -1))),
                        DamageRecipient.ACTIVE_PLAYER));
    }
}
