package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentOnEnterEffect;

@CardRegistration(set = "4ED", collectorNumber = "299")
@CardRegistration(set = "2ED", collectorNumber = "234")
@CardRegistration(set = "SUM", collectorNumber = "236")
@CardRegistration(set = "3ED", collectorNumber = "236")
@CardRegistration(set = "V10", collectorNumber = "2")
@CardRegistration(set = "ME3", collectorNumber = "191")
@CardRegistration(set = "MPS", collectorNumber = "32")
@CardRegistration(set = "MB2", collectorNumber = "139")
@CardRegistration(set = "LEA", collectorNumber = "233")
@CardRegistration(set = "LEB", collectorNumber = "234")
public class BlackVise extends Card {

    public BlackVise() {
        addEffect(EffectSlot.STATIC, new ChooseOpponentOnEnterEffect());
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(
                        new Sum(new CardsInHand(CountScope.TARGET_PLAYER), new Fixed(-4)),
                        DamageRecipient.CHOSEN_PLAYER));
    }
}
