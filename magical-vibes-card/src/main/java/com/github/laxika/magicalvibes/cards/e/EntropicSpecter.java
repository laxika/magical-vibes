package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "EXO", collectorNumber = "61")
public class EntropicSpecter extends Card {

    public EntropicSpecter() {
        CardsInHand chosenOpponentHand = new CardsInHand(CountScope.OPPONENTS);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(chosenOpponentHand, chosenOpponentHand));

        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
    }
}
